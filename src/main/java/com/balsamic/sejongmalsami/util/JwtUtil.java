package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtil {
  @Value("${jwt.secret-key}")
  private String secretKey; // JWT 비밀 키
  @Value("${jwt.access-exp-time}")
  private Long accessTokenExpTime; // 액세스 토큰 만료 시간
  @Value("${jwt.refresh-exp-time}")
  private Long refreshTokenExpTime; // 리프레시 토큰 만료 시간
  @Value("${jwt.issuer}")
  private String issuer; // JWT 발급자

  private static final String ROLE = "role"; // 클레임 키

  /**
   * 액세스 토큰 생성
   *
   * @param customUserDetails 사용자 상세 정보
   * @return 생성된 액세스 토큰
   */
  public String createAccessToken(CustomUserDetails customUserDetails) {
    log.info("액세스 토큰 생성 중: 사용자 '{}'", customUserDetails.getUsername());
    return createToken(customUserDetails, accessTokenExpTime);
  }

  /**
   * 리프레시 토큰 생성
   *
   * @param customUserDetails 사용자 상세 정보
   * @return 생성된 리프레시 토큰
   */
  public String createRefreshToken(CustomUserDetails customUserDetails) {
    log.info("리프레시 토큰 생성 중: 사용자 '{}'", customUserDetails.getUsername());
    return createToken(customUserDetails, refreshTokenExpTime);
  }

  /**
   * JWT 토큰 생성 메서드
   *
   * @param customUserDetails 사용자 상세 정보
   * @param expiredAt         만료 시간
   * @return 생성된 JWT 토큰
   */
  private String createToken(CustomUserDetails customUserDetails, Long expiredAt) {
    Date now = new Date();
    Map<String, Object> headers = new HashMap<>();
    headers.put("typ", "JWT");

    return Jwts.builder()
        .setHeader(headers)
        .setIssuer(issuer)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + expiredAt))
        .setSubject(customUserDetails.getUsername())
        .claim(ROLE, customUserDetails.getMember().getRole())
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /**
   * JWT 토큰 유효성 검사
   *
   * @param token 검증할 JWT 토큰
   * @return 유효 여부
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(getSigningKey()) // 변경된 디코더 사용
          .build()
          .parseClaimsJws(token);
      log.info("JWT 토큰이 유효합니다.");
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.warn("형식이 잘못된 JWT 토큰입니다: {}", e.getMessage());
    } catch (SignatureException e) {
      log.warn("JWT 서명이 유효하지 않습니다: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.warn("JWT 토큰이 비어있거나 null입니다: {}", e.getMessage());
    }
    return false;
  }

  /**
   * JWT 토큰에서 클레임(Claims) 추출
   *
   * @param token JWT 토큰
   * @return 추출된 클레임
   */
  public Claims getClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey()) // 변경된 디코더 사용
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * JWT 서명에 사용할 키 생성
   *
   * @return SecretKey 객체
   */
  private SecretKey getSigningKey() {
    try {
      byte[] keyBytes = Decoders.BASE64URL.decode(secretKey); // BASE64URL 디코더로 변경
      return Keys.hmacShaKeyFor(keyBytes);
    } catch (IllegalArgumentException e) {
      log.error("비밀 키 디코딩 실패: {}", e.getMessage());
      throw e; // 예외 재발생
    }
  }

  /**
   * 리프레시 토큰 만료 시간 반환
   *
   * @return 리프레시 토큰 만료 시간 (밀리초 단위)
   */
  public long getRefreshExpirationTime() {
    return refreshTokenExpTime;
  }
}
