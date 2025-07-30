package com.balsamic.sejongmalsami.web.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
public class FirebaseConfig {

  @Value("${firebase.config-path}")
  private String firebaseConfigPath;

  @Value("${firebase.config.api-key}")
  private String firebaseApiKey;

  @Value("${firebase.config.auth-domain}")
  private String firebaseAuthDomain;

  @Value("${firebase.config.project-id}")
  private String firebaseProjectId;

  @Value("${firebase.config.storage-bucket}")
  private String firebaseStorageBucket;

  @Value("${firebase.config.messaging-sender-id}")
  private String firebaseMessagingSenderId;

  @Value("${firebase.config.app-id}")
  private String firebaseAppId;

  @Value("${firebase.config.measurement-id}")
  private String firebaseMeasurementID;

  @Value("${firebase.config.vapid-key}")
  private String firebaseVapidKey;

  @PostConstruct
  public void initialize() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      log.info("Firebase 초기화를 시작합니다.");
      try (InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream(firebaseConfigPath)) {
        if (serviceAccount == null) {
          log.error("firebase 설정 json 파일을 찾을 수 없습니다.");
          throw new IOException("Firebase 서비스 계정 키 파일이 존재하지 않습니다.");
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build();

        FirebaseApp.initializeApp(options);
        log.info("Firebase가 성공적으로 초기화되었습니다.");
      } catch (IOException e) {
        log.error("Firebase 초기화 중 오류가 발생했습니다: {}", e.getMessage());
        throw e;
      }
    } else {
      log.info("Firebase는 이미 초기화되어 있습니다.");
    }
  }
}
