package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebLoginDto {
  private boolean success;
  private String accessToken;
  private String message;
}