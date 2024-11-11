package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.AuthCommand;
import com.balsamic.sejongmalsami.object.AuthDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.AuthService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<Void> validatePageToken(
      @ModelAttribute AuthCommand command){
    String accessToken = command.getAccessToken();
    System.out.println("validateToken 호출됨. accessToken: " + accessToken); // 추가 로그

    if (accessToken == null || accessToken.isEmpty()) {
      System.out.println("accessToken이 비어있음.");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    boolean isValidAccessToken = jwtUtil.validateToken(accessToken);
    System.out.println("accessToken 유효성 검증 결과: " + isValidAccessToken); // 추가 로그

    if(isValidAccessToken) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }


  @PostMapping(value = "/logout")
  @LogMonitoringInvocation
  @Override
  public ResponseEntity<Void> logout(HttpServletResponse response) {
    // 리프레시 토큰 쿠키 삭제
    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);
    return ResponseEntity.ok().build();
  }

  /*
   인증된 사용자 확인
   */
  private Boolean isValidateUserDetails(CustomUserDetails customUserDetails){
    return customUserDetails != null && customUserDetails.getMemberId() != null;
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
