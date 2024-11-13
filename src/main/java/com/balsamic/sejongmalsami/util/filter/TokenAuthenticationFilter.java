package com.balsamic.sejongmalsami.util.filter;

import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.config.SecurityUrls;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
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

  private final JwtUtil jwtUtil; // JWT 유틸리티
  private final MemberService memberService; // 멤버 서비스
  private static final AntPathMatcher pathMatcher = new AntPathMatcher(); // URL 패턴 매처

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String uri = request.getRequestURI();
    log.debug("요청된 URI: {}", uri);

    // 화이트리스트 체크 : 화이트리스트 경로면 필터링 건너뜀
    if (isWhitelistedPath(uri)) {
      filterChain.doFilter(request, response);
      return;
    }

    // 요청 타입 구분 : API 요청인지 관리자 페이지 요청인지
    boolean isApiRequest = uri.startsWith("/api/");
    boolean isAdminPage = uri.startsWith("/admin/");

    try {
      String token = null;

      // 토큰 추출 : 요청 타입에 따라 헤더 또는 파라미터에서 토큰 추출
      if (isApiRequest) {
        // API 요청 : Authorization 헤더에서 Bearer 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
          token = bearerToken.substring(7).trim(); // "Bearer " 제거
        }
      } else if (isAdminPage) {
        // 관리자 페이지 : accessToken 파라미터에서 토큰 추출
        token = request.getParameter("accessToken");
      }

      // 토큰 검증 : 토큰이 유효하면 인증 설정
      if (token != null && jwtUtil.validateToken(token)) {
        Authentication authentication = memberService.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 관리자 페이지 권한 체크 : 관리자 권한 없으면 로그인 페이지로 리다이렉트
        if (isAdminPage && !hasAdminRole(authentication)) {
          response.sendRedirect("/login");
          return;
        }

        // 인증 성공 : 필터 체인 계속 진행
        filterChain.doFilter(request, response);
        return;
      }

      // 토큰 없거나 유효하지 않음
      if (isApiRequest) {
        // API 요청 : 예외 던짐
        throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
      } else if (isAdminPage) {
        // 관리자 페이지 : 로그인 페이지로 리다이렉트
        response.sendRedirect("/login");
      }

    } catch (ExpiredJwtException e) {
      // 토큰 만료 예외 처리
      if (isApiRequest) {
        throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
      } else {
        response.sendRedirect("/login");
      }
    } catch (Exception e) {
      // 기타 예외 처리
      if (isApiRequest) {
        throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
      } else {
        response.sendRedirect("/login");
      }
    }
  }

  /**
   * 관리자 권한 확인
   *
   * @param authentication 인증 정보
   * @return 관리자 권한 여부
   */
  private boolean hasAdminRole(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
  }

  /**
   * 화이트리스트 경로 확인
   *
   * @param uri 요청된 URI
   * @return 화이트리스트 여부
   */
  private boolean isWhitelistedPath(String uri) {
    return SecurityUrls.AUTH_WHITELIST.stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, uri));
  }
}
