package com.balsamic.sejongmalsami.web.config;

import com.balsamic.sejongmalsami.util.properties.VertexAiProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.genai.Client;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;

/**
 * Vertex AI 임베딩 클라이언트 설정 (fallback 용)
 *
 * <p>임베딩 메인 엔진은 SUH-AIder 이고, Vertex AI 는 fallback 이므로
 * {@code @Lazy} 로 지연 생성한다. credentials 설정이 없어도 앱 기동에는 영향을 주지 않으며,
 * 실제 fallback 호출 시점에만 초기화된다.
 */
@Configuration
@RequiredArgsConstructor
public class VertexAiConfig {

  private final VertexAiProperties vertexAiProperties;

  @Bean
  @Lazy
  public Client embeddingClient() throws IOException {
    try (InputStream inputStream =
             new ClassPathResource(vertexAiProperties.getCredentialsFile()).getInputStream()) {
      GoogleCredentials credentials = ServiceAccountCredentials
          .fromStream(inputStream)
          .createScoped(List.of(vertexAiProperties.getCloudPlatformUrl()));

      return Client.builder()
          .vertexAI(vertexAiProperties.getUseVertexAi())
          .project(vertexAiProperties.getProjectId())
          .location(vertexAiProperties.getEmbeddingLocation())
          .credentials(credentials)
          .build();
    }
  }
}
