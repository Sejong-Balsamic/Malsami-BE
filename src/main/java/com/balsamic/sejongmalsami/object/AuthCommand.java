package com.balsamic.sejongmalsami.object;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthCommand {
  private String refreshToken;
  private String accessToken;
  private UUID memberId;
}