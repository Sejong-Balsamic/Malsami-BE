package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.genai")
public class GoogleGenAiProperties {
  private boolean useVertexAi;
  private String model;
  private String projectId;
  private String location;
  private String credentialsFile;
  private String cloudPlatformUrl;
}
