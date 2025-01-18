package com.balsamic.sejongmalsami.object;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthDto {
  private String accessToken;
  private Boolean isValidToken;
  private String studentName;
  private UUID memberId;
}
