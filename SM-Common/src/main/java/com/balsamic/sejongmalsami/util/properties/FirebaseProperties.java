package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

  private String configPath;
  private String apiKey;
  private String authDomain;
  private String projectId;
  private String storageBucket;
  private String messagingSenderId;
  private String appId;
  private String measurementId;
  private String vapidKey;
}