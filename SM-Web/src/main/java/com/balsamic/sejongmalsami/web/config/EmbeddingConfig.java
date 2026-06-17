package com.balsamic.sejongmalsami.web.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.genai.Client;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Getter
public class EmbeddingConfig {

  // google.genai.* 설정은 더 이상 운영에서 사용하지 않으므로, 값이 없어도
  // 컨텍스트 초기화가 실패하지 않도록 placeholder 에 기본값(빈 값)을 부여한다.
  @Value("${google.genai.project-id:}")
  private String projectId;

  @Value("${google.genai.location:}")
  private String location;

  @Value("${google.genai.credentials-file:}")
  private String credentialsFile;

  @Value("${google.genai.use-vertex-ai:false}")
  private Boolean useVertexAi;

  @Value("${google.genai.model:}")
  private String model;

  @Value("${google.genai.cloud-platform-url:}")
  private String cloudPlatformUrl;

  // 설정이 없으면 임베딩 기능은 호출 시점에만 실패하도록 @Lazy 로 지연 생성한다.
  // (앱 기동 자체는 영향을 받지 않는다)
  @Bean
  @Lazy
  public Client genAiClient() throws IOException {
    // JSON 파일 로드
    ClassPathResource classPathResource = new ClassPathResource(credentialsFile);
    try (InputStream inputStream = classPathResource.getInputStream()) {

      // 서비스 계정 로드
      GoogleCredentials googleCredentials =
          ServiceAccountCredentials.fromStream(inputStream)
              .createScoped(List.of(cloudPlatformUrl));

      return Client.builder()
          .vertexAI(useVertexAi)
          .project(projectId)
          .location(location)
          .credentials(googleCredentials)
          .build();
    }
  }
}
