package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.util.CourseFileGenerator;
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
    log.info("====== Server 시작: StartupRunner 실행 ======");
    // Course 파싱
    courseFileGenerator.initCourse();
    // 저장된 Course -> Subject 파싱
    courseFileGenerator.initSubject();
  }
}
