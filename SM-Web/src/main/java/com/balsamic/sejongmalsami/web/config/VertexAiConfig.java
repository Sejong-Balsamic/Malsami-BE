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
import org.springframework.core.io.ClassPathResource;

/**
 * Vertex AI 임베딩 클라이언트 설정 (fallback 용)
 *
 * <p>임베딩 메인 엔진은 SUH-AIder 이고, Vertex AI 는 fallback 이다.
 * credentials JSON 은 배포 시 VERTEX_CREDENTIALS_JSON secret 으로 생성되므로
 * 빈을 즉시 생성한다. (com.google.genai.Client 는 final 클래스라 @Lazy 프록시가 불가능하다)
 */
@Configuration
@RequiredArgsConstructor
public class VertexAiConfig {

  private final VertexAiProperties vertexAiProperties;

  @Bean
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
