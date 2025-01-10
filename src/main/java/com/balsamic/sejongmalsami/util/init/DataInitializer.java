package com.balsamic.sejongmalsami.util.init;

import static com.balsamic.sejongmalsami.util.log.LogUtil.lineLog;
import static com.balsamic.sejongmalsami.util.log.LogUtil.lineLogError;

import com.balsamic.sejongmalsami.object.constants.HashType;
import com.balsamic.sejongmalsami.service.HashRegistryService;
import com.balsamic.sejongmalsami.service.SejongAcademicService;
import com.balsamic.sejongmalsami.util.CommonUtil;
import com.balsamic.sejongmalsami.util.TimeUtil;
import com.balsamic.sejongmalsami.util.config.ServerConfig;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

  private final CourseFileGenerator courseFileGenerator;
  private final DepartmentService departmentService;
  private final SubjectService subjectService;
  private final SejongAcademicService sejongAcademicService;
  private final ServerErrorCodeService serverErrorCodeService;
  private final HashRegistryService hashRegistryService;

  @Value("${ftp.path.courses}")
  private Path coursesPath;

  @Value("${ftp.path.department}")
  private Path departmentsPath;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    lineLog("SERVER START");
    lineLog("데이터 초기화 시작");
    LocalDateTime overallStartTime = LocalDateTime.now();

    // ServerConfig 초기화 및 정의
    initServerConfig();

    // Department 파싱 동기적 실행
    String departmentHash;
    try {
      departmentService.initDepartments();
      // Department 해시값 계산
      departmentHash = CommonUtil.calculateFileHash(ServerConfig.departmentPath);
    } catch (Exception e) {
      lineLogError("서버 시작 DB 세팅 중 오류 발생");
      log.error("서버 시작 DB 세팅 중 오류 발생", e);
      lineLogError(null);
      throw e; // 애플리케이션 시작 중단
    }

    // Course 파싱 비동기 실행
    CompletableFuture<Void> courseFuture = CompletableFuture.runAsync(() -> {
          courseFileGenerator.initCourses();
    });

    // Course 파싱 완료 후 Subject 처리 조건 확인
    CompletableFuture<Void> subjectFuture = courseFuture.thenRun(() -> {
      try {
        // 현재 Course 해시값 (모든 CourseFile의 해시를 결합하여 생성)
        String currentCourseHash = courseFileGenerator.getCombinedCourseHash();

        // 이전 해시값 조회
        String previousDeptHash = hashRegistryService.getHashValue(HashType.DEPARTMENT_JSON);
        String previousCourseHash = hashRegistryService.getHashValue(HashType.COURSE_FILES);

        boolean isDeptChanged = !departmentHash.equals(previousDeptHash);
        boolean isCourseChanged = !currentCourseHash.equals(previousCourseHash);

        if (isDeptChanged || isCourseChanged) {
          log.info("Department 또는 Course가 변경되었으므로 Subject를 재파싱합니다.");
          subjectService.processDistinctSubjects();

          // 해시값 업데이트
          hashRegistryService.updateHashValue(HashType.DEPARTMENT_JSON, departmentHash);
          hashRegistryService.updateHashValue(HashType.COURSE_FILES, currentCourseHash);
        } else {
          log.info("Department 및 Course에 변화가 없으므로 Subject 파싱을 건너뜁니다.");
        }

        // ServerErrorCode 해시값 확인 및 업데이트
        String currentErrorCodeHash = serverErrorCodeService.calculateErrorCodeHash();
        String previousErrorCodeHash = hashRegistryService.getHashValue(HashType.SERVER_ERROR_CODES);

        if (!currentErrorCodeHash.equals(previousErrorCodeHash)) {
          log.info("ServerErrorCode가 변경되었으므로 업데이트합니다.");
          serverErrorCodeService.initErrorCodes();
          hashRegistryService.updateHashValue(HashType.SERVER_ERROR_CODES, currentErrorCodeHash);
        } else {
          log.info("ServerErrorCode에 변화가 없으므로 업데이트를 건너뜁니다.");
        }

      } catch (Exception e) {
        throw new RuntimeException("Subject 파싱 중 오류 발생", e);
      }
    });

    subjectFuture
        .thenRun(() -> {
          lineLog("Department, Course, Subject 세팅 완료");
        })
        .exceptionally(e -> {
          lineLogError("Department, Course, Subject 세팅중 오류 발생");
          log.error("Department, Course, Subject 세팅중 오류 발생", e);
          return null;
        });

    // 비동기로 실행 -> 작업완료까지 대기
    subjectFuture.get();

    // Department, Course, Subject, Faculty 초기화 작업이 끝나고, isActive 핸들링
    manageDataActiveStatus();

    LocalDateTime overallEndTime = LocalDateTime.now();
    Duration overallDuration = Duration.between(overallStartTime, overallEndTime);
    lineLog(null);
    lineLog("서버 데이터 초기화 및 업데이트 완료");
    log.info("총 소요 시간: {}", TimeUtil.convertDurationToReadableTime(overallDuration));
    lineLog(null);
  }

  private void initServerConfig() {
    ServerConfig.coursePath = determineCoursePath();
    ServerConfig.departmentPath = determineDepartmentFilePath();
  }

  private void manageDataActiveStatus() {
    lineLog("세종대학교 학술 정보 : 상태 핸들링 시작");

    // Department 작업
    // processDepartmentIsActive();

    // Course 작업
    // sejongAcademicService.processCourseIsActive();

    // Faculty 작업
    sejongAcademicService.processFacultyIsActive();

    // Subject 작업
    // sejongAcademicService.processSubjectIsActive();

    lineLog("세종대학교 학술 정보 : 상태 핸들링 완료");
  }

  /**
   * SystemType 에 따라 departments.json 파일 Path 반환
   */
  private Path determineDepartmentFilePath() {
    if (ServerConfig.isLinuxServer) {
      // 서버 환경
      log.info("서버 환경: departments.json 경로 설정됨 = {}", departmentsPath);
    } else {
      // 로컬 환경: src/main/resources/departments.json
      try {
        departmentsPath = Paths.get(
            getClass().getClassLoader().getResource("departments.json").toURI()
        );
        log.info("로컬 환경: departments.json 경로 설정됨 = {}", departmentsPath);
      } catch (Exception e) {
        log.error("로컬 환경에서 departments.json 파일을 찾을 수 없습니다.", e);
        throw new RuntimeException("departments.json 파일을 찾을 수 없습니다.", e);
      }
    }
    return departmentsPath;
  }

  /**
   * SystemType 에 따라 CourseFile Path 반환
   */
  private Path determineCoursePath() {
    if (ServerConfig.isLinuxServer) {
      // 서버 환경
    } else {
      // 로컬 환경: build/resources/main/courses/ 경로
      try {
        coursesPath = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("courses/")).toURI());
      } catch (Exception e) {
        log.error("로컬 환경에서 courses 디렉토리를 찾을 수 없습니다.", e);
        throw new RuntimeException("courses 디렉토리를 찾을 수 없습니다.", e);
      }
    }
    return coursesPath;
  }
}
