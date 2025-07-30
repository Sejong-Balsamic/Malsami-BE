package com.balsamic.sejongmalsami.application.init;

import static com.balsamic.sejongmalsami.util.log.LogUtil.lineLog;

import com.balsamic.sejongmalsami.academic.object.postgres.CourseFile;
import com.balsamic.sejongmalsami.academic.repository.postgres.CourseFileRepository;
import com.balsamic.sejongmalsami.constants.FileStatus;
import com.balsamic.sejongmalsami.dto.ServerInfo;
import com.balsamic.sejongmalsami.object.postgres.Subject;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import com.balsamic.sejongmalsami.util.CommonUtil;
import com.balsamic.sejongmalsami.util.TimeUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 교과목 파일과 교과목명을 초기화하는 유틸리티 클래스입니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CourseFileGenerator {

  private final CourseService courseService;
  private final CourseFileRepository courseFileRepository;
  private final SubjectRepository subjectRepository;

  /**
   * 교과목 파일을 초기화합니다.
   */
  public void initCourses() {
    lineLog("Course 초기화 시작");
    log.info("Course 교과목 XLSX 파일 처리 시작 = {}", TimeUtil.readableCurrentLocalDateTime());

    // coursePath 불러오기
    Path coursesPath = ServerInfo.coursePath;
    log.info("ServerCoursePath: {}", coursesPath);

    // XLSX 파일 목록 수집
    List<Path> xlsxFiles = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(coursesPath, "*.xlsx")) {
      for (Path filePath : stream) {
        log.info("파일 경로: {}", filePath);
        xlsxFiles.add(filePath);
      }
    } catch (Exception e) {
      log.error("교과목파일 스캔 중 오류 발생", e);
      return;
    }

    // 파일 목록 로깅
    log.info("처리할 파일 목록:");
    xlsxFiles.forEach(filePath -> log.info(filePath.getFileName().toString()));

    LocalDateTime overallStartTime = LocalDateTime.now();
    log.info("처리 시작 시간: {}", TimeUtil.readableLocalDateTime(overallStartTime));

    int successCount = 0;
    int failCount = 0;
    int passCount = 0;
    int totalAddedCourses = 0;

    for (Path filePath : xlsxFiles) {
      try {
        int addedCourses = processFile(filePath);
        if (addedCourses > 0) { // 성공적으로 처리된 파일
          successCount++;
          totalAddedCourses += addedCourses;
        } else if (addedCourses == 0) { // 패스한 파일
          passCount++;
        }
        // addedCourses < 0 은 현재 처리 중인 파일으로 간주하여 카운트하지 않음
      } catch (Exception e) {
        log.error("파일 처리 중 예외 발생: {}", filePath.getFileName().toString(), e);
        failCount++;
      }
    }

    LocalDateTime endTime = LocalDateTime.now();
    Duration overallDuration = Duration.between(overallStartTime, endTime);
    lineLog("Course 실행 결과");
    log.info("처리 종료 시간: {}", TimeUtil.readableLocalDateTime(endTime));
    log.info("총 소요 시간: {}", TimeUtil.convertDurationToReadableTime(overallDuration));
    log.info("성공적으로 처리된 파일 수: {}", successCount);
    log.info("실패한 파일 수: {}", failCount);
    log.info("패스한 파일 수: {}", passCount);
    log.info("추가된 교과목 수: {}", totalAddedCourses);
    lineLog("Course 처리 완료");
  }

  /**
   * 교과목명을 초기화합니다.
   * 기존 Course 엔티티에서 중복 없는 subject 이름을 추출하여 Subject 테이블에 저장합니다.
   */
  @Transactional
  public void initSubject() {
    lineLog("Subject 확인 시작");
    log.info("중복 없는 교과목명 초기화 시작 = {}", LocalDateTime.now());

    // 중복 없는 교과목명 목록 조회
    List<String> distinctSubjects = courseService.getDistinctSubjects();

    int addedSubjects = 0;
    for (String subjectName : distinctSubjects) {
      // 이미 존재하는 교과목명인지 확인
      if (!subjectRepository.existsByName(subjectName)) {
        // 새로운 Subject 엔티티 생성 및 저장
        Subject subject = Subject.builder()
            .name(subjectName)
            .dailyDocumentScore(0L)
            .weeklyDocumentScore(0L)
            .monthlyDocumentScore(0L)
            .totalDocumentScore(0L)
            .dailyQuestionScore(0L)
            .weeklyQuestionScore(0L)
            .monthlyQuestionScore(0L)
            .totalQuestionScore(0L)
            .build();
        subjectRepository.save(subject);
        addedSubjects++;
        log.info("새로운 Subject 추가됨: {}", subjectName);
      } else {
        log.info("이미 존재하는 Subject: {}", subjectName);
      }
    }

    log.info("Subject 처리 완료: 추가된 교과목 수 = {}", addedSubjects);
  }

  /**
   * 파일을 처리하여 Course 엔티티에 저장합니다.
   *
   * @param filePath 처리할 파일 경로
   * @return 추가된 교과목 수
   */
  @Transactional
  public int processFile(Path filePath) {
    File targetFile = filePath.toFile();
    String targetFileName = targetFile.getName();

    // 이미 처리된 파일인지 확인
    CourseFile existingFile = courseFileRepository.findByFileName(targetFileName).orElse(null);
    if (existingFile != null) {
      if (existingFile.getFileStatus() == FileStatus.SUCCESS) {
        log.info("이미 성공적으로 처리된 파일: {}", targetFileName);
        return 0;
      } else if (existingFile.getFileStatus() == FileStatus.PENDING
          || existingFile.getFileStatus() == FileStatus.FAILURE) {

        // 파일 Status가 PENDING이거나 FAILURE인 경우
        log.info("재처리 대상 파일: {}", targetFileName);

        // 기존 교과목 데이터 삭제
        Integer year = existingFile.getYear();
        Integer semester = existingFile.getSemester();
        if (year != null && semester != null) {
          courseService.deleteCoursesByYearAndSemester(year, semester);
          log.info("기존 교과목 삭제됨: 년도: {}, 학기: {}", year, semester);
        }

        // CourseFile -> PENDING 상태로 업데이트
        existingFile.setFileStatus(FileStatus.PENDING);
        existingFile.setErrorMessage(null);
        existingFile.setProcessedAt(LocalDateTime.now());
        existingFile.setDurationSeconds(null);
        courseFileRepository.save(existingFile);
        log.info("파일을 PENDING 상태로 업데이트: {}", targetFileName);
      }
    } else {
      // 새로운 파일인 경우
      // 파일 이름에서 year와 semester 추출
      String[] parts = Objects.requireNonNull(targetFileName).split("-");
      if (parts.length < 3 || !parts[0].equals("course")) {
        log.error("파일 이름 -> 잘못된 구조: {}", targetFileName);
        // CourseFile에 실패 기록 추가 저장
        courseFileRepository.save(
            CourseFile.builder()
                .fileName(targetFileName)
                .processedAt(LocalDateTime.now())
                .fileStatus(FileStatus.FAILURE)
                .filePath(filePath.toString())
                .errorMessage("파일 이름 -> 잘못된 파일 이름 형식")
                .build());
        return 0;
      }

      Integer year;
      Integer semester;
      try {
        year = Integer.parseInt(parts[1]);
        semester = Integer.parseInt(parts[2].split("\\.")[0]);
      } catch (Exception e) {
        log.error("파일 이름 -> 년도 또는 학기 추출 실패: {}", targetFileName, e);
        // CourseFile에 실패 기록 추가 저장
        courseFileRepository.save(CourseFile.builder()
            .fileName(targetFileName)
            .processedAt(LocalDateTime.now())
            .fileStatus(FileStatus.FAILURE)
            .errorMessage("파일 이름 -> 년도 또는 학기 추출 실패")
            .build());
        return 0;
      }

      // PENDING 상태 -> CourseFile 생성 저장
      CourseFile courseFile = CourseFile.builder()
          .fileName(targetFileName)
          .year(year)
          .semester(semester)
          .processedAt(LocalDateTime.now())
          .fileStatus(FileStatus.PENDING)
          .filePath(filePath.toString())
          .build();
      courseFileRepository.save(courseFile);
      log.info("현재 작업중인 파일 : PENDING : {}", targetFileName);
    }

    // 실행 시간 초기화
    LocalDateTime fileStartTime = LocalDateTime.now();

    // 파일 처리 시작
    try {
      int addedCourses = courseService.parseAndSaveCourses(targetFile);

      // 실행 시간 계산
      LocalDateTime fileEndTime = LocalDateTime.now();
      Duration duration = Duration.between(fileStartTime, fileEndTime);
      long durationSeconds = duration.getSeconds();

      // CourseFile 상태를 SUCCESS로 업데이트
      if (existingFile != null) {
        existingFile.setFileStatus(FileStatus.SUCCESS);
        existingFile.setProcessedAt(fileEndTime);
        existingFile.setDurationSeconds(durationSeconds);
        courseFileRepository.save(existingFile);
      } else {
        // 새로운 파일인 경우 (이미 PENDING 상태로 저장됨)
        // 최근에 저장된 CourseFile 조회
        CourseFile newCourseFile = courseFileRepository.findByFileName(targetFileName)
            .orElseThrow(() -> new CustomException(ErrorCode.COURSE_SAVE_ERROR));
        newCourseFile.setFileStatus(FileStatus.SUCCESS);
        newCourseFile.setProcessedAt(fileEndTime);
        newCourseFile.setDurationSeconds(durationSeconds);
        courseFileRepository.save(newCourseFile);
      }

      log.info("파일 처리 성공: {}", targetFileName);
      return addedCourses;
    } catch (Exception e) {
      // CourseFile을 FAILURE 상태로 변경 후 저장

      // 실행 시간 계산
      LocalDateTime fileEndTime = LocalDateTime.now();
      Duration duration = Duration.between(fileStartTime, fileEndTime);
      long durationSeconds = duration.getSeconds();

      log.error("파일 처리 중 오류 발생: {}", targetFileName, e);
      if (existingFile != null) {
        existingFile.setFileStatus(FileStatus.FAILURE);
        existingFile.setErrorMessage(e.getMessage());
        existingFile.setProcessedAt(fileEndTime);
        existingFile.setDurationSeconds(durationSeconds);
        courseFileRepository.save(existingFile);
      } else {
        // 새로운 파일인 경우
        CourseFile newCourseFile = courseFileRepository.findByFileName(targetFileName)
            .orElseThrow(() -> new CustomException(ErrorCode.COURSE_SAVE_ERROR));
        newCourseFile.setFileStatus(FileStatus.FAILURE);
        newCourseFile.setErrorMessage(e.getMessage());
        newCourseFile.setProcessedAt(fileEndTime);
        newCourseFile.setDurationSeconds(durationSeconds);
        courseFileRepository.save(newCourseFile);
      }

      return 0;
    }
  }

  /**
   * 모든 성공한 CourseFile의 해시를 결합하여 하나의 해시를 생성합니다.
   *
   * @return 결합된 해시값 문자열
   */
  public String getCombinedCourseHash() {
    try {
      List<CourseFile> successfulCourseFiles = courseFileRepository.findByFileStatus(FileStatus.SUCCESS);

      // 파일 이름을 정렬하여 순서를 고정
      List<String> fileHashes = successfulCourseFiles.stream()
          .sorted(Comparator.comparing(CourseFile::getFileName))
          .map(cf -> {
            try {
              Path path = ServerInfo.coursePath.resolve((cf.getFileName()));
              if (Files.exists(path)) {
                return CommonUtil.calculateFileHash(path);
              } else {
                log.warn("CourseFile 경로가 존재하지 않습니다: {}", path);
                return "";
              }
            } catch (Exception e) {
              log.error("CourseFile 해시 계산 중 오류 발생: {}", cf.getFileName(), e);
              return "";
            }
          })
          .filter(hash -> !hash.isEmpty())
          .collect(Collectors.toList());

      String concatenatedHashes = String.join("", fileHashes);
      return CommonUtil.calculateSha256ByStr(concatenatedHashes);
    } catch (Exception e) {
      log.error("Combined Course Hash 계산 중 오류 발생", e);
      return "";
    }
  }
}
