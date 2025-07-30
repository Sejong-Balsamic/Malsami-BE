package com.balsamic.sejongmalsami.auth.dto;

import com.balsamic.sejongmalsami.mongo.FcmToken;
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
}
