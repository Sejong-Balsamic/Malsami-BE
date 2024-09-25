package com.balsamic.sejongmalsami.util.config;

import jakarta.servlet.MultipartConfigElement;
import java.io.File;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class MultipartConfig {

  @Bean
  public MultipartResolver multipartResolver() {
    return new StandardServletMultipartResolver(); // Spring Boot 기본 MultipartResolver 사용
  }

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    // 최대 업로드 파일 크기 설정 (30MB)
    factory.setMaxFileSize(DataSize.ofMegabytes(30));
    // 요청 전체에 대한 최대 크기 설정 (30MB)
    factory.setMaxRequestSize(DataSize.ofMegabytes(30));
    // 메모리 상에서 처리할 최대 파일 크기 (1MB)
    factory.setFileSizeThreshold(DataSize.ofMegabytes(1));
    // 임시 파일을 저장할 경로 설정
    String tempDir = System.getProperty("user.dir") + "/src/main/resources/temp";
    File file = new File(tempDir);
    // 경로가 없으면 디렉토리를 생성
    if (!file.exists()) {
      file.mkdirs();
    }
    // 임시 파일 저장 경로 설정
    factory.setLocation(tempDir);
    return factory.createMultipartConfig();
  }
}