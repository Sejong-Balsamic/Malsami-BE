package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SUH-AIder(자체 AI 서버) 설정 (suh.aider.*)
 *
 * <p>임베딩 생성의 메인 엔진. 설정 값이 없어도 컨텍스트는 정상 기동된다.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "suh.aider")
public class SuhAiderProperties {

  private String baseUrl;
  private Security security;
  private Embedding embedding;

  @Getter
  @Setter
  public static class Security {
    private String apiKey;
  }

  @Getter
  @Setter
  public static class Embedding {
    private String defaultModel;
  }
}
