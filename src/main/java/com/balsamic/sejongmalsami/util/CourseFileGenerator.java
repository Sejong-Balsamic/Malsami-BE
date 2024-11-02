package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.postgres.CourseFile;
import com.balsamic.sejongmalsami.repository.postgres.CourseFileRepository;
import com.balsamic.sejongmalsami.service.CourseService;
import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

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
    log.info("서버 시작 시 Course 파일 처리 시작");

    Path path = Paths.get(COURSE_FILES_DIR);
    if (!Files.exists(path) || !Files.isDirectory(path)) {
      log.warn("Course 파일 디렉토리가 존재하지 않습니다: {}", COURSE_FILES_DIR);
      return;
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.xlsx")) {
      for (Path filePath : stream) {
        File file = filePath.toFile();
        String fileName = file.getName();

        // 이미 처리된 파일인지 확인
        if (courseFileRepository.findByFileName(fileName).isPresent()) {
          log.info("이미 처리된 파일: {}", fileName);
          continue;
        }

        // 파일 이름에서 year와 semester 추출
        String[] parts = Objects.requireNonNull(fileName).split("-");
        if (parts.length < 3 || !parts[0].equals("course")) {
          log.error("잘못된 파일 이름 구조: {}", fileName);
          // `CourseFile`에 실패 기록 추가
          courseFileRepository.save(CourseFile.builder()
              .fileName(fileName)
              .processedAt(LocalDateTime.now())
              .success(false)
              .errorMessage("잘못된 파일 형식")
              .build());
          continue;
        }

        Integer year;
        Integer semester;
        try {
          year = Integer.parseInt(parts[1]);
          semester = Integer.parseInt(parts[2].split("\\.")[0]);
        } catch (Exception e) {
          log.error("년도 또는 학기 추출 실패: {}", fileName, e);
          // `CourseFile 정보 추출 실패
          courseFileRepository.save(CourseFile.builder()
              .fileName(fileName)
              .processedAt(LocalDateTime.now())
              .success(false)
              .errorMessage("년도 또는 학기 추출 실패")
              .build());
          continue;
        }

        // 파일 처리
        try {
          // File을 MultipartFile로 변환하지 않고, File을 직접 처리
          courseService.parseAndSaveCourses(file);
          // `CourseFile`에 성공 기록 추가
          courseFileRepository.save(CourseFile.builder()
              .fileName(fileName)
              .year(year)
              .semester(semester)
              .processedAt(LocalDateTime.now())
              .success(true)
              .build());
          log.info("파일 처리 성공: {}", fileName);
        } catch (Exception e) {
          log.error("파일 처리 중 오류 발생: {}", fileName, e);
          // `CourseFile`에 실패 기록 추가
          courseFileRepository.save(CourseFile.builder()
              .fileName(fileName)
              .year(year)
              .semester(semester)
              .processedAt(LocalDateTime.now())
              .success(false)
              .errorMessage(e.getMessage())
              .build());
        }
      }
    } catch (Exception e) {
      log.error("파일 스캔 중 오류 발생", e);
    }

    log.info("서버 시작 시 Course 파일 처리 완료");
  }
}
