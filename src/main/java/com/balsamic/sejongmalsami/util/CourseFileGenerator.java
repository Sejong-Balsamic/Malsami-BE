package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.postgres.CourseFile;
import com.balsamic.sejongmalsami.object.constants.FileStatus;
import com.balsamic.sejongmalsami.repository.postgres.CourseFileRepository;
import com.balsamic.sejongmalsami.service.CourseService;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseFileGenerator implements ApplicationRunner {

  private final CourseService courseService;
  private final CourseFileRepository courseFileRepository;

  //FIXME: 임시 하드코딩
  private static final String COURSE_FILES_DIR = "src/main/resources/courses/";

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("============== Course File Generator ==============");
    log.info("Course 교과목 XLSX 파일 처리 시작 = {}", LocalDateTime.now());
    //TODO: 서버 업로드시 서버내 경로에서 확인
    log.info("교과목파일 저장 디렉토리 = {}", COURSE_FILES_DIR);

    Path path = Paths.get(COURSE_FILES_DIR);
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      log.warn("Course 파일 디렉토리가 존재하지 않습니다: {}", COURSE_FILES_DIR);
      return;
    }

    List<Path> xlsxFiles = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xlsx")) {
      for (Path filePath : stream) {
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
    log.info("처리 시작 시간: {}", overallStartTime);

    int successCount = 0;
    int failCount = 0;
    int totalAddedCourses = 0;

    for (Path filePath : xlsxFiles) {
      try {
        int addedCourses = processFile(filePath);
        if (addedCourses > 0) { // 성공적으로 처리된 파일
          successCount++;
          totalAddedCourses += addedCourses;
        } else if (addedCourses == 0) { // 실패한 파일
          failCount++;
        }
        // addedCourses < 0 은 현재 처리 중인 파일로 간주하여 카운트하지 않음
      } catch (Exception e) {
        log.error("파일 처리 중 예외 발생: {}", filePath.getFileName().toString(), e);
        failCount++;
      }
    }

    LocalDateTime endTime = LocalDateTime.now();
    Duration overallDuration = Duration.between(overallStartTime, endTime);
    log.info("처리 종료 시간: {}", endTime);
    log.info("총 소요 시간: {}초", overallDuration.getSeconds());
    log.info("성공적으로 처리된 파일 수: {}", successCount);
    log.info("실패한 파일 수: {}", failCount);
    log.info("추가된 교과목 수: {}", totalAddedCourses);

    log.info("서버 시작 시 Course 파일 처리 완료");
  }

  @Transactional
  public int processFile(Path filePath) {
    File file = filePath.toFile();
    String fileName = file.getName();

    // 이미 처리된 파일인지 확인
    CourseFile existingFile = courseFileRepository.findByFileName(fileName).orElse(null);
    if (existingFile != null) {
      if (existingFile.getFileStatus() == FileStatus.SUCCESS) {
        log.info("이미 성공적으로 처리된 파일: {}", fileName);
        return 0;
      } else if (existingFile.getFileStatus() == FileStatus.PENDING || existingFile.getFileStatus() == FileStatus.FAILURE) {

        // 파일 Status 가 PENDING 이거나 FAILURE 인 경우
        log.info("재처리 대상 파일: {}", fileName);

        // 기존 교과목 데이터 삭제
        Integer year = existingFile.getYear();
        Integer semester = existingFile.getSemester();
        if (year != null && semester != null) {
          courseService.deleteCoursesByYearAndSemester(year, semester);
          log.info("기존 교과목 삭제됨: 년도: {}, 학기: {}", year, semester);
        }

        // CourseFile -> PENDING
        existingFile.setFileStatus(FileStatus.PENDING);
        existingFile.setErrorMessage(null);
        existingFile.setProcessedAt(LocalDateTime.now());
        existingFile.setDurationSeconds(null);
        courseFileRepository.save(existingFile);
        log.info("파일을 PENDING 상태로 업데이트: {}", fileName);
      }
    } else {
      // 새로운 파일인 경우
      // 파일 이름에서 year와 semester 추출
      String[] parts = Objects.requireNonNull(fileName).split("-");
      if (parts.length < 3 || !parts[0].equals("course")) {
        log.error("파일 이름 -> 잘못된 구조: {}", fileName);
        // CourseFile 에 실패 기록 추가 저장
        courseFileRepository.save(
            CourseFile.builder()
            .fileName(fileName)
            .processedAt(LocalDateTime.now())
            .fileStatus(FileStatus.FAILURE)
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
        log.error("파일 이름 -> 년도 또는 학기 추출 실패: {}", fileName, e);
        // `CourseFile`에 실패 기록 추가
        courseFileRepository.save(CourseFile.builder()
            .fileName(fileName)
            .processedAt(LocalDateTime.now())
            .fileStatus(FileStatus.FAILURE)
            .errorMessage("파일 이름 -> 년도 또는 학기 추출 실패")
            .build());
        return 0;
      }

      // PENDING 상태 -> CourseFile 생성 저장
      CourseFile courseFile = CourseFile.builder()
          .fileName(fileName)
          .year(year)
          .semester(semester)
          .processedAt(LocalDateTime.now())
          .fileStatus(FileStatus.PENDING)
          .build();
      courseFileRepository.save(courseFile);
      log.info("현재 작업중인 파일 : PENDING : {}", fileName);
    }

    // 실행 시간 init
    LocalDateTime fileStartTime = LocalDateTime.now();

    // 파일 처리 시작
    try {
      int addedCourses = courseService.parseAndSaveCourses(file);

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
        // Find the recently saved CourseFile
        CourseFile newCourseFile = courseFileRepository.findByFileName(fileName)
            .orElseThrow(() -> new CustomException(ErrorCode.COURSE_SAVE_ERROR));
        newCourseFile.setFileStatus(FileStatus.SUCCESS);
        newCourseFile.setProcessedAt(fileEndTime);
        newCourseFile.setDurationSeconds(durationSeconds);
        courseFileRepository.save(newCourseFile);
      }

      log.info("파일 처리 성공: {}", fileName);
      return addedCourses;
    } catch (Exception e) {
      // CourseFile -> FAILURE 변경 후 저장

      // 실행 시간 계산
      LocalDateTime fileEndTime = LocalDateTime.now();
      Duration duration = Duration.between(fileStartTime, fileEndTime);
      long durationSeconds = duration.getSeconds();

      log.error("파일 처리 중 오류 발생: {}", fileName, e);
      if (existingFile != null) {
        existingFile.setFileStatus(FileStatus.FAILURE);
        existingFile.setErrorMessage(e.getMessage());
        existingFile.setProcessedAt(fileEndTime);
        existingFile.setDurationSeconds(durationSeconds);
        courseFileRepository.save(existingFile);
      } else {
        // 새로운 파일
        CourseFile newCourseFile = courseFileRepository.findByFileName(fileName)
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
}
