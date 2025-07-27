package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.AuthCommand;
import com.balsamic.sejongmalsami.object.AuthDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.WebLoginDto;
import com.balsamic.sejongmalsami.object.mongo.RefreshToken;
import com.balsamic.sejongmalsami.repository.mongo.RefreshTokenRepository;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final MemberService memberService;
  private final CustomUserDetailsService customUserDetailsService;
  private final FcmTokenService fcmTokenService;
  private final JwtUtil jwtUtil;
  private final RefreshTokenRepository refreshTokenRepository;

  /**
   * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
   */
  @Transactional
  public AuthDto refreshAccessToken(AuthCommand command) {
    String refreshToken = command.getRefreshToken();
    // 리프레시 토큰 검증 (JWT 유효성 검사)
    if (!jwtUtil.validateToken(refreshToken)) {
      log.error("리프레시 토큰이 유효하지 않습니다.");
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰이 데이터베이스에 존재하는지 확인
    RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> {
          log.error("저장된 리프레시 토큰을 찾을 수 없습니다.");
          return new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        });

    // 리프레시 토큰 만료 여부 확인
    if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      log.error("리프레시 토큰이 만료되었습니다.");
      refreshTokenRepository.deleteByToken(refreshToken); // 만료된 토큰 삭제
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰에서 사용자 정보 추출
    Claims claims = jwtUtil.getClaims(refreshToken);
    String username = claims.getSubject();

    // 사용자 정보 로드
    CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // 새로운 액세스 토큰 생성
    String newAccessToken = jwtUtil.createAccessToken(userDetails);

    log.info("새로운 AccessToken 발급 완료: 회원 = {}", userDetails.getMember().getStudentId());
    log.info("새로운 AccessToken: {}", newAccessToken);

    return AuthDto.builder()
        .accessToken(newAccessToken)
        .studentName(userDetails.getMember().getStudentName()) //2024.11.24 : SUH : 학생 이름 반환 추가
        .memberId(userDetails.getMemberId()) //2024.11.27 : FE#166 : 키 추가 요청
        .build();
  }

  /**
   * 모바일 전용 토큰 갱신 (Access + Refresh 둘 다 재발급)
   */
  @Transactional
  public AuthDto refreshTokensForMobile(AuthCommand command) {
    String oldRefreshToken = command.getRefreshToken();
    
    // 리프레시 토큰 검증 (JWT 유효성 검사)
    if (!jwtUtil.validateToken(oldRefreshToken)) {
      log.error("모바일: 리프레시 토큰이 유효하지 않습니다.");
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰이 데이터베이스에 존재하는지 확인
    RefreshToken storedToken = refreshTokenRepository.findByToken(oldRefreshToken)
        .orElseThrow(() -> {
          log.error("모바일: 저장된 리프레시 토큰을 찾을 수 없습니다.");
          return new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        });

    // 리프레시 토큰 만료 여부 확인
    if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      log.error("모바일: 리프레시 토큰이 만료되었습니다.");
      refreshTokenRepository.deleteByToken(oldRefreshToken); // 만료된 토큰 삭제
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰에서 사용자 정보 추출
    Claims claims = jwtUtil.getClaims(oldRefreshToken);
    String username = claims.getSubject();

    // 사용자 정보 로드
    CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // 새로운 액세스 토큰 및 리프레시 토큰 생성
    String newAccessToken = jwtUtil.createAccessToken(userDetails);
    String newRefreshToken = jwtUtil.createRefreshToken(userDetails);

    // 기존 리프레시 토큰 삭제
    refreshTokenRepository.deleteByToken(oldRefreshToken);
    log.info("모바일: 기존 리프레시 토큰 삭제 완료: 회원 = {}", userDetails.getMember().getStudentId());

    // 새로운 리프레시 토큰 저장
    RefreshToken newRefreshTokenEntity = RefreshToken.builder()
        .token(newRefreshToken)
        .memberId(userDetails.getMemberId())
        .expiryDate(jwtUtil.getRefreshExpiryDate())
        .build();
    refreshTokenRepository.save(newRefreshTokenEntity);
    log.info("모바일: 새로운 리프레시 토큰 저장 완료: 회원 = {}", userDetails.getMember().getStudentId());

    log.info("모바일: 새로운 AccessToken 및 RefreshToken 발급 완료: 회원 = {}", userDetails.getMember().getStudentId());

    return AuthDto.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .studentName(userDetails.getMember().getStudentName())
        .memberId(userDetails.getMemberId())
        .build();
  }

  /**
   * 관리자 페이지 로그인
   */
  public WebLoginDto webLogin(MemberCommand command, HttpServletResponse response) {
    log.info("관리자 로그인 시도: {}", command.getSejongPortalId());
    try {
      // 회원 로그인 사용
      MemberDto memberDto = memberService.signIn(command, response);
      log.info("로그인 결과: isAdmin={}, studentID={}", memberDto.getIsAdmin(), memberDto.getMember().getStudentId());

      // 관리자가 아닐시
      if(!memberDto.getIsAdmin()){
        log.warn("관리자 권한 없음: {}", command.getSejongPortalId());
        return WebLoginDto.builder()
            .success(false)
            .message("로그인에 실패했습니다. 관리자가 아닙니다")
            .build();
      }

      // 관리자인 경우
      log.info("관리자 로그인 성공: {}", command.getSejongPortalId());
      return WebLoginDto.builder()
          .success(true)
          .accessToken(memberDto.getAccessToken())
          .build();

    } catch (Exception e) {
      // 세종 로그인 실패
      log.error("로그인 실패: {}", command.getSejongPortalId(), e);
      return WebLoginDto.builder()
          .success(false)
          .message("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.")
          .build();
    }
  }

  /**
   * 로그아웃 처리
   */
  @Transactional
  public void logout(AuthCommand command, HttpServletRequest request, HttpServletResponse response) {
    // FCM 토큰 삭제
    fcmTokenService.deleteFcmToken(command);

    // 리프레시 토큰 추출
    String refreshToken = extractRefreshTokenFromCookies(request.getCookies());

    // 리프레시 토큰이 존재하는지 확인
    if (!refreshTokenRepository.findByToken(refreshToken).isPresent()) {
      log.error("삭제할 리프레시 토큰을 찾을 수 없습니다: {}", refreshToken);
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰 삭제
    refreshTokenRepository.deleteByToken(refreshToken);
    log.info("리프레시 토큰 삭제 완료: {}", refreshToken);

    // 리프레시 토큰 쿠키 삭제
    deleteRefreshTokenCookie(response);

    // AccessToken 삭제
    response.setHeader("Authorization", "Bearer ");
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

  /**
   * 리프레시 토큰 쿠키 삭제
   */
  private void deleteRefreshTokenCookie(HttpServletResponse response) {
    Cookie deleteCookie = new Cookie("refreshToken", null);
    deleteCookie.setMaxAge(0);
    deleteCookie.setPath("/");
    deleteCookie.setHttpOnly(true);
    deleteCookie.setSecure(false);  //FIXME: 개발 환경 false, 프로덕션 true

    // 쿠키에 SameSite 속성 추가 : Cookie 객체에서 미지원하므로 수동으로 set-Cookie 헤더에 추가
    StringBuilder cookieBuilder = new StringBuilder();
    cookieBuilder.append(deleteCookie.getName()).append("=").append(deleteCookie.getValue()).append(";");
    cookieBuilder.append(" Path=").append(deleteCookie.getPath()).append(";");
    cookieBuilder.append(" Max-Age=0;"); // 쿠키 삭제
    cookieBuilder.append(" SameSite=None;"); //FIXME: 모든 요청에서 쿠키 전송
    cookieBuilder.append(" Secure;"); //FIXME: 개발 환경 false, 프로덕션 true

    if (deleteCookie.isHttpOnly()) {
      cookieBuilder.append(" HttpOnly;");
    }

    String setCookieHeader = cookieBuilder.toString();
    response.addHeader("Set-Cookie", setCookieHeader);

    log.info("로그아웃 : Set-Cookie Header (삭제): {}", setCookieHeader);
  }
}
