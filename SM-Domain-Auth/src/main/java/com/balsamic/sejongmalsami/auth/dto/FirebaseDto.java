package com.balsamic.sejongmalsami.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FirebaseDto {
  private String firebaseApiKey;
  private String firebaseAuthDomain;
  private String firebaseProjectId;
  private String firebaseStorageBucket;
  private String firebaseMessagingSenderId;
  private String firebaseAppId;
  private String firebaseMeasurementId;
  private String firebaseVapidKey;
}
