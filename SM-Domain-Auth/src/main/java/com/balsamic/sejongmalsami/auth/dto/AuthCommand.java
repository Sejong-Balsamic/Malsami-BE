package com.balsamic.sejongmalsami.auth.dto;

import com.balsamic.sejongmalsami.object.postgres.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;
  private String fcmToken;
  
  // 세종포털 로그인 정보
  private String sejongPortalId;
  private String sejongPortalPassword;
}