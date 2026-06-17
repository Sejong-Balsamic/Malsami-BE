package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Vertex AI 임베딩/생성 설정 (vertex.ai.*)
 *
 * <p>임베딩은 SUH-AIder 를 우선 사용하고, 실패 시 Vertex AI 로 fallback 한다.
 * 설정 값이 없어도 컨텍스트는 정상 기동되며, fallback 호출 시점에만 영향을 받는다.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vertex.ai")
public class VertexAiProperties {

  private String projectId;
  private String credentialsFile;
  private int dimension;
  private Boolean useVertexAi;
  private String cloudPlatformUrl;

  private String embeddingLocation;
  private String embeddingModel;

  private String generationLocation;
  private String generationModel;
}
