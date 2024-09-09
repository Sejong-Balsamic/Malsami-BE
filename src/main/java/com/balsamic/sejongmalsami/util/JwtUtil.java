package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.MemberService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {
  private final MemberService memberService;

  @Value("${jwt.secret-key}")
  private String secretKey;
  @Value("${jwt.access-exp-time}")
  private Long accessTokenExpTime;
  @Value("${jwt.refresh-exp-time}")
  private Long refreshTokenExpTime;

  private static final String ROLE = "role";

  // 액세스 토큰 생성
  public String createAccessToken(CustomUserDetails customUserDetails) {
    log.info("액세스 토큰 생성 중: 사용자 '{}'", customUserDetails.getUsername());
    return createToken(customUserDetails, accessTokenExpTime);
  }

  // 리프레시 토큰 생성
  public String createRefreshToken(CustomUserDetails customUserDetails) {
    log.info("리프레시 토큰 생성 중: 사용자 '{}'", customUserDetails.getUsername());
    return createToken(customUserDetails, refreshTokenExpTime);
  }

  // JWT 토큰 생성 메서드
  private String createToken(CustomUserDetails customUserDetails, Long expiredAt) {
    Date now = new Date();
    Map<String, Object> headers = new HashMap<>();
    headers.put("typ", "JWT");

    String token = Jwts.builder()
        .setHeader(headers)
        .setIssuer("SULGAMEWIKI")
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expiredAt))
        .setSubject(customUserDetails.getUsername())
        .claim(ROLE, customUserDetails.getMember().getRole())
        .signWith(getSigningKey())
        .compact();

    log.info("JWT 토큰 생성 완료: 사용자 '{}', 만료시간 '{}'", customUserDetails.getUsername(), expiredAt);
    return token;
  }

  // JWT 토큰 유효성 검사
  public boolean validateToken(String token) {
    try {
      Jwts.parser()
          .setSigningKey(getSigningKey())
          .parseClaimsJws(token);
      log.info("JWT 토큰이 유효합니다.");
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.warn("잘못된 JWT 서명입니다: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT 토큰이 비어있습니다: {}", e.getMessage());
    }
    return false;
  }

  // JWT 토큰에서 인증 정보 추출
  public Authentication getAuthentication(String token) {
    log.info("JWT 토큰에서 인증 정보 추출 중");
    Claims claims = getClaims(token);

    Set<SimpleGrantedAuthority> authorities =
        Collections.singleton(new SimpleGrantedAuthority(claims.get(ROLE, String.class)));

    CustomUserDetails customUserDetails =
        memberService.loadUserByUsername(claims.getSubject());

    log.info("인증 정보 추출 완료: 사용자 '{}'", customUserDetails.getUsername());
    return new UsernamePasswordAuthenticationToken(customUserDetails, token, authorities);
  }

  // JWT 토큰에서 클레임(Claims) 추출
  public Claims getClaims(String token) {
    log.info("JWT 토큰에서 클레임(Claims) 추출 중");
    return Jwts.parser()
        .setSigningKey(getSigningKey())
        .parseClaimsJws(token)
        .getBody();
  }

  // JWT 서명에 사용할 키 생성
  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
