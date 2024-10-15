package com.balsamic.sejongmalsami.util.filter;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
  private final MemberService memberService;
  private final List<String> whitelist;
  private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

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

    String token = getAccessToken(request);
    log.debug("JWT 토큰 추출: {}", (token != null) ? "토큰 존재" : "토큰 없음");

    if (token != null) {
      try {
        if (jwtUtil.validateToken(token)) {
          log.info("JWT 토큰 유효성 검사 성공.");

          // MemberService를 통해 Authentication 객체 가져오기
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
          log.error("유효하지 않은 JWT 토큰입니다: {}", uri);
          throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
        }
      } catch (io.jsonwebtoken.ExpiredJwtException e) {
        log.error("JWT 토큰이 만료되었습니다: {}", e.getMessage());
        throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
      } catch (io.jsonwebtoken.UnsupportedJwtException e) {
        log.error("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
      } catch (io.jsonwebtoken.MalformedJwtException e) {
        log.error("형식이 잘못된 JWT 토큰입니다: {}", e.getMessage());
        throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
      } catch (io.jsonwebtoken.SignatureException e) {
        log.error("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
        throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN);
      } catch (IllegalArgumentException e) {
        log.error("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
        throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
      } catch (Exception e) {
        log.error("JWT 처리 중 오류 발생: {}", e.getMessage());
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    } else {
      log.error("Authorization 헤더에 JWT 토큰이 없습니다: {}", uri);
      throw new CustomException(ErrorCode.MISSING_AUTH_TOKEN);
    }

    // 다음 필터로 요청 전달
    filterChain.doFilter(request, response);
  }

  // URI가 화이트리스트에 포함되는지 확인
  private boolean isWhitelistedPath(String uri) {
    boolean matched = whitelist.stream().anyMatch(path -> ANT_PATH_MATCHER.match(path, uri));
    log.debug("URI '{}'가 화이트리스트에 포함되는지: {}", uri, matched);
    return matched;
  }

  // HTTP 요청에서 Authorization 헤더에서 Bearer 토큰 추출
  private String getAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      String token = bearerToken.substring(7).trim();
      log.debug("Bearer 토큰 추출 완료.");
      return token;
    }
    log.debug("Authorization 헤더에 Bearer 토큰이 없거나 형식이 잘못되었습니다.");
    return null;
  }
}
