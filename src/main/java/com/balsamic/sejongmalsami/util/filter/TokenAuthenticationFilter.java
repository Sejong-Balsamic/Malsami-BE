package com.balsamic.sejongmalsami.util.filter;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.constants.SecurityUrls;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 토큰 기반 인증 필터
 */
@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;                                                   // JWT 유틸리티
  private final MemberService memberService;                                       // 회원 서비스
  private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();    // URL 패턴 매칭 유틸리티

  /**
   * 필터 처리 메서드
   *
   * @param request     HTTP 요청
   * @param response    HTTP 응답
   * @param filterChain 필터 체인
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    log.debug("요청된 URI: {}", uri);

    // 화이트리스트에 있는 URL은 JWT 검증을 생략
    if (isWhitelistedPath(uri)) {
      log.debug("URI '{}'는 화이트리스트에 포함되어 JWT 검증을 생략합니다.", uri);
      filterChain.doFilter(request, response);
      return;
    }

    // 토큰 추출 및 검증
    String token = getAccessToken(request);
    log.debug("JWT 토큰 추출: {}", (token != null) ? "토큰 존재" : "토큰 없음");

    if (token != null) {
      try {
        validateAndSetAuthentication(token);
      } catch (CustomException e) {
        // 웹 요청인 경우 로그인 페이지로 리다이렉트
        if (isWebRequest(uri)) {
          response.sendRedirect("/login");
          return;
        }
        throw e;
      }
    } else {
      if (isWebRequest(uri)) {
        response.sendRedirect("/login");
        return;
      }
      log.error("Authorization 헤더에 JWT 토큰이 없습니다: {}", uri);
      throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
    }

    filterChain.doFilter(request, response);
  }

  /**
   * 토큰 검증 및 인증 정보 설정
   *
   * @param token JWT 토큰
   */
  private void validateAndSetAuthentication(String token) {
    try {
      if (jwtUtil.validateToken(token)) {
        log.info("JWT 토큰 유효성 검사 성공.");

        Authentication authentication = memberService.getAuthentication(token);
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
          SecurityContextHolder.getContext().setAuthentication(authentication);
          String username = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
          log.info("SecurityContext에 '{}' 회원 인증 정보 설정 완료.", username);
        } else {
          log.error("Authentication 객체가 null이거나 CustomUserDetails 타입이 아닙니다.");
          throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
      } else {
        log.error("유효하지 않은 JWT 토큰입니다");
        throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
      }
    } catch (io.jsonwebtoken.JwtException e) {
      log.error("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
    }
  }

  /**
   * URI가 화이트리스트에 포함되는지 확인
   *
   * @param uri 검사할 URI
   * @return 화이트리스트 포함 여부
   */
  private boolean isWhitelistedPath(String uri) {
    boolean matched = SecurityUrls.AUTH_WHITELIST.stream()
        .anyMatch(path -> ANT_PATH_MATCHER.match(path, uri));
    log.debug("URI '{}'가 화이트리스트에 포함되는지: {}", uri, matched);
    return matched;
  }

  /**
   * HTTP 요청에서 Bearer 토큰 추출
   *
   * @param request HTTP 요청
   * @return 추출된 토큰 또는 null
   */
  private String getAccessToken(HttpServletRequest request) {
    // API 요청의 경우 헤더에서 토큰 추출
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7).trim();
      log.debug("Bearer 토큰 추출 완료.");
      return token;
    }

    // 웹 요청의 경우 파라미터에서 토큰 추출
    String paramToken = request.getParameter("accessToken");
    if (paramToken != null) {
      log.debug("파라미터에서 토큰 추출 완료.");
      return paramToken;
    }

    log.debug("토큰을 찾을 수 없습니다.");
    return null;
  }

  /**
   * 웹 요청인지 확인
   *
   * @param uri 요청 URI
   * @return 웹 요청 여부
   */
  private boolean isWebRequest(String uri) {
    return uri.startsWith("/admin/") || uri.startsWith("/web/");
  }
}