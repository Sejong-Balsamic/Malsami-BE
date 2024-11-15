package com.balsamic.sejongmalsami.util.config;

import static com.balsamic.sejongmalsami.util.LogUtils.lineLog;
import static com.balsamic.sejongmalsami.util.LogUtils.lineLogError;

import com.balsamic.sejongmalsami.util.CourseFileGenerator;
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

  // 애플리케이션이 시작될 때 실행
  @Override
  public void run(ApplicationArguments args) throws Exception {
    lineLog("SERVER START");
    lineLog("데이터 초기화 시작");

    LocalDateTime overallStartTime = LocalDateTime.now();

    // 비동기 : Course 파싱
    CompletableFuture<Void> courseFuture = CompletableFuture.runAsync(() -> {
      courseFileGenerator.initCourse();
    });

    // 비동기 : Course 저장 -> 이후 -> Subject 저장
    courseFuture.thenRun(() -> {
      courseFileGenerator.initSubject();
    }).thenRun(() -> {
      LocalDateTime overallEndTime = LocalDateTime.now();
      Duration overallDuration = Duration.between(overallStartTime, overallEndTime);

      lineLog("DB 세팅 완료");
      log.info("총 소요 시간: {}초", overallDuration.getSeconds());
      lineLog(null);

    }).exceptionally(e -> {

      lineLogError("서버 시작중 오류 발생");
      log.error("서버 시작 DB 세팅 중 오류 발생", e);
      lineLogError(null);

      return null;
    });
  }
}
