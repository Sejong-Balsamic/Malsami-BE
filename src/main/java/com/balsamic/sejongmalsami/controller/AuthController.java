package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.AuthCommand;
import com.balsamic.sejongmalsami.object.AuthDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.AuthService;
import com.balsamic.sejongmalsami.service.FcmTokenService;
import com.balsamic.sejongmalsami.util.JwtUtil;
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

  private final AuthService authService;
  private final JwtUtil jwtUtil;
  private final FcmTokenService fcmTokenService;

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
