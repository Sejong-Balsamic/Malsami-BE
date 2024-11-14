package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.util.CourseFileGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

  private final CourseFileGenerator courseFileGenerator;

  // 애플리케이션이 시작될 때 실행
  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("====== Server 시작: DataInitializer 실행 ======");
    LocalDateTime overallStartTime = LocalDateTime.now();

    // 초기화 시작 로그 추가
    log.info("데이터 초기화를 시작합니다.");

    // 비동기 : Course 파싱
    CompletableFuture<Void> courseFuture = CompletableFuture.runAsync(() -> {
      log.info("Course 초기화를 시작합니다.");
      courseFileGenerator.initCourse();
      log.info("Course 초기화가 완료되었습니다.");
    });

    // 비동기 : Course 저장 -> 이후 -> Subject 저장
    courseFuture.thenRun(() -> {
      log.info("Subject 저장 시작");
      courseFileGenerator.initSubject();
      log.info("Subject 초기화 완료");
    }).thenRun(() -> {
      LocalDateTime overallEndTime = LocalDateTime.now();
      Duration overallDuration = Duration.between(overallStartTime, overallEndTime);
      log.info("============ DB 세팅 완료 ==========");
      log.info("총 소요 시간: {}초", overallDuration.getSeconds());
      log.info("==================================");
    }).exceptionally(e -> {
      log.error("서버 시작 DB 세팅 중 오류 발생", e);
      return null;
    });
  }
}
