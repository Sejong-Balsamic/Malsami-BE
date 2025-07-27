package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.FcmToken;
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
