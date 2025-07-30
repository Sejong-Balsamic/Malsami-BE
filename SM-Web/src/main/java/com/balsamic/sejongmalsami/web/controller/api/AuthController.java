package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.application.service.AuthApplicationService;
import com.balsamic.sejongmalsami.auth.dto.AuthCommand;
import com.balsamic.sejongmalsami.auth.dto.AuthDto;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.auth.service.FcmTokenService;
import com.balsamic.sejongmalsami.member.dto.MemberDto;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(
    name = "인증 관리 API",
    description = "인증 관련 API 제공"
)
public class AuthController implements AuthControllerDocs {

  private final AuthApplicationService authService;
  private final FcmTokenService fcmTokenService;

  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/signin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> signIn(
      @ModelAttribute AuthCommand command, HttpServletResponse response) {
    return ResponseEntity.ok(authService.signIn(command, response));
  }

  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/mobile/signin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> signInForMobile(
      @ModelAttribute AuthCommand command) {
    return ResponseEntity.ok(authService.signInForMobile(command));
  }

  @PostMapping(value = "/refresh")
  @LogMonitoringInvocation
  @Override
  public ResponseEntity<AuthDto> refreshAccessToken(
      HttpServletRequest request) {
    AuthDto authDto = authService.refreshAccessToken(
        AuthCommand.builder()
            .refreshToken(extractRefreshTokenFromCookies(request.getCookies()))
            .build());
    return ResponseEntity.ok(authDto);
  }

  /**
   * 모바일 전용 토큰 갱신 (Access + Refresh 둘 다 재발급)
   */
  @PostMapping(value = "/mobile/refresh", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  @Override
  public ResponseEntity<AuthDto> refreshTokensForMobile(
      @ModelAttribute AuthCommand command) {
    AuthDto authDto = authService.refreshTokensForMobile(command);
    return ResponseEntity.ok(authDto);
  }

  @PostMapping(value = "/logout", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  @Override
  public ResponseEntity<Void> logout(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AuthCommand command,
      HttpServletRequest request,
      HttpServletResponse response) {
    command.setMember(customUserDetails.getMember());
    authService.logout(command, request, response);
    return ResponseEntity.ok().build();
  }

  @Override
  @PostMapping(value = "/fcm/token", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AuthDto> saveFcmToken(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AuthCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(fcmTokenService.saveFcmToken(command));
  }

  /**
   * 쿠키에서 리프레시 토큰 추출
   */
  private String extractRefreshTokenFromCookies(Cookie[] cookies) {
    if (cookies == null) {
      throw new CustomException(ErrorCode.MISSING_REFRESH_TOKEN);
    }
    for (Cookie cookie : cookies) {
      if ("refreshToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    throw new CustomException(ErrorCode.MISSING_REFRESH_TOKEN);
  }
}
