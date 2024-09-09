package com.balsamic.sejongmalsami.util.filter;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;
  private final List<String> whitelist;
  private final AntPathMatcher antPathMatcher = new AntPathMatcher();

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String URI = request.getRequestURI();
    log.info("요청된 URI: {}", URI);

    // 화이트리스트에 있는 URL은 JWT 검증을 생략
    if (isWhitelistedPath(URI)) {
      log.info("화이트리스트에 포함된 경로이므로 JWT 검증을 생략합니다: {}", URI);
      filterChain.doFilter(request, response);
      return;
    }

    String token = getAccessToken(request);
    log.info("JWT 토큰 추출: {}", token != null ? "토큰 존재" : "토큰 없음");

    if (token != null && jwtUtil.validateToken(token)) {
      log.info("JWT 토큰 유효성 검사 성공");

      Authentication authentication = jwtUtil.getAuthentication(token);
      if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("SecurityContext에 '{}' 사용자 인증 정보 설정 완료", ((CustomUserDetails) authentication.getPrincipal()).getUsername());
      } else {
        log.warn("Authentication 객체가 null이거나 CustomUserDetails 타입이 아닙니다.");
      }
    } else {
      log.warn("유효하지 않은 JWT 토큰입니다: {}", URI);
    }

    filterChain.doFilter(request, response);
  }

  private boolean isWhitelistedPath(String uri) {
    return whitelist.stream().anyMatch(path -> antPathMatcher.match(path, uri));
  }

  private String getAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
