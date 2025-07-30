package com.balsamic.sejongmalsami.auth.dto;

import com.balsamic.sejongmalsami.auth.object.mongo.FcmToken;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthDto {
  private String accessToken;
  private String refreshToken;
  private Boolean isValidToken;
  private String studentName;
  private UUID memberId;
  private FcmToken fcmToken;
  
  // 세종포털 인증 정보
  private String major;
  private String studentIdString;
  private String academicYear;
  private String enrollmentStatus;
}
