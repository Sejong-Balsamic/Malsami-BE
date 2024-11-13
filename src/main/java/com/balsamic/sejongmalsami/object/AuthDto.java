package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthDto {
  private String accessToken;
  private Boolean isValidToken;
}
