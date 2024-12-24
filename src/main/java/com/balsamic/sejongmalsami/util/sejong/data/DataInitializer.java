package com.balsamic.sejongmalsami.util.sejong.data;

import static com.balsamic.sejongmalsami.util.LogUtil.lineLog;
import static com.balsamic.sejongmalsami.util.LogUtil.lineLogError;

import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.service.SejongAcademicService;
import com.balsamic.sejongmalsami.util.FileUtil;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  // 애플리케이션이 시작될 때 실행
  @Override
  public void run(ApplicationArguments args) throws Exception {
    lineLog("SERVER START");
    lineLog("데이터 초기화 시작");

    LocalDateTime overallStartTime = LocalDateTime.now();

    // 1. Department 파싱 동기적 실행
    try {
      Path deptPath = determineDepartmentFilePath();
      departmentService.loadDepartments(deptPath);
    } catch (Exception e) {
      lineLogError("서버 시작 DB 세팅 중 오류 발생");
      log.error("서버 시작 DB 세팅 중 오류 발생", e);
      lineLogError(null);
      throw e; // 애플리케이션 시작 중단
    }

    // 2. Course 파싱 비동기 실행
    CompletableFuture<Void> courseFuture = CompletableFuture.runAsync(() -> {
      courseFileGenerator.initCourse();
    });

    // 3. Course 파싱 완료 후 Subject 처리
    CompletableFuture<Void> subjectFuture = courseFuture.thenRun(() -> {
      subjectService.processDistinctSubjects();
    });

    subjectFuture
        .thenRun(() -> {
          LocalDateTime overallEndTime = LocalDateTime.now();
          Duration overallDuration = Duration.between(overallStartTime, overallEndTime);

          lineLog("DB 세팅 완료");
          log.info("총 소요 시간: {}초", overallDuration.getSeconds());
          lineLog(null);
        })
        .exceptionally(e -> {
          lineLogError("서버 시작중 오류 발생");
          log.error("서버 시작 DB 세팅 중 오류 발생", e);
          lineLogError(null);
          return null;
        });

    // 비동기로 실행 -> 작업완료까지 대기
    subjectFuture.get();

    // 4. 모든 초기화 작업이 끝나고, isActive 핸들링
    manageDataActiveStatus();
  }

  /**
   * 시스템 타입에 따라 departments.json 파일의 경로를 결정합니다.
   *
   * @return departments.json 파일의 Path
   */
  private Path determineDepartmentFilePath() {
    SystemType systemType = FileUtil.getCurrentSystem();
    Path deptPath;

    switch (systemType) {
      case LINUX:
        // 서버 환경: /mnt/sejong-malsami/department/departments.json
        deptPath = Paths.get("/mnt/sejong-malsami/department/departments.json");
        log.info("서버 환경: departments.json 경로 설정됨 = {}", deptPath);
        break;
      case WINDOWS:
      case MAC:
      case OTHER:
      default:
        // 로컬 환경: src/main/resources/departments.json
        try {
          deptPath = Paths.get(
              getClass().getClassLoader().getResource("departments.json").toURI()
          );
          log.info("로컬 환경: departments.json 경로 설정됨 = {}", deptPath);
        } catch (Exception e) {
          log.error("로컬 환경에서 departments.json 파일을 찾을 수 없습니다.", e);
          throw new RuntimeException("departments.json 파일을 찾을 수 없습니다.", e);
        }
        break;
    }

    return deptPath;
  }

  private void manageDataActiveStatus() {
    lineLog("세종대학교 학술 정보 : 상태 핸들링 시작");

    // Department 작업
//    processDepartmentIsActive();

    // Course 작업
//  sejongAcademicService.processCourseIsActive();

    // Faculty 작업
    sejongAcademicService.processFacultyIsActive();

    // Subject 작업
//  sejongAcademicService.processSubjectIsActive();

    lineLog("세종대학교 학술 정보 : 상태 핸들링 완료");
  }
}
