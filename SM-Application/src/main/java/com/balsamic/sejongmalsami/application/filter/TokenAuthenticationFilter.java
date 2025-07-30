package com.balsamic.sejongmalsami.application.filter;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.auth.service.CustomUserDetailsService;
import com.balsamic.sejongmalsami.application.JwtUtil;
import com.balsamic.sejongmalsami.config.SecurityUrls;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService customUserDetailsService;
  private static final AntPathMatcher pathMatcher = new AntPathMatcher();

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
        // 관리자 페이지 : Authorization 헤더 우선 확인
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
          token = bearerToken.substring(7).trim();
        } else {
          // Authorization 헤더에 토큰이 없을 경우 파라미터 확인
          String paramToken = request.getParameter("accessToken");
          if (paramToken != null && !paramToken.isEmpty()) {
            token = paramToken;
          }
        }
      }

      // 토큰 검증 : 토큰이 유효하면 인증 설정
      if (token != null && jwtUtil.validateToken(token)) {
        String username = jwtUtil.getUsernameFromToken(token);
        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
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
        // 토큰없음
        if (token == null) {
          sendErrorResponse(response, ErrorCode.MISSING_AUTH_TOKEN);
        } else { // 유효안함
          sendErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
        }
        return; // 필터 체인 진행하지 않음
      } else if (isAdminPage) {
        // 관리자 페이지 : 로그인 페이지로 리다이렉트
        response.sendRedirect("/login");
        return;
      }

    } catch (ExpiredJwtException e) {
      log.warn("토큰 만료 처리: {}", e.getMessage());
      // 토큰 만료 예외 처리
      if (isApiRequest) {
        sendErrorResponse(response, ErrorCode.EXPIRED_ACCESS_TOKEN);
      } else {
        response.sendRedirect("/login");
      }
      return;
    } catch (Exception e) {
      // 기타 예외 처리
      if (isApiRequest) {
        sendErrorResponse(response, ErrorCode.INVALID_ACCESS_TOKEN);
      } else {
        response.sendRedirect("/login");
      }
      return;
    }

    // 필터 체인 계속 진행
    filterChain.doFilter(request, response);
  }

  /**
   * 에러 응답을 JSON 형태로 클라이언트에 전송
   *
   * @param response   HttpServletResponse 객체
   * @param errorCode  발생한 에러 코드
   * @throws IOException
   */
  private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(errorCode.getStatus().value());
    response.setCharacterEncoding("UTF-8");

    ErrorResponse errorResponse = new ErrorResponse(errorCode, errorCode.getMessage());

    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(response.getWriter(), errorResponse);
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
