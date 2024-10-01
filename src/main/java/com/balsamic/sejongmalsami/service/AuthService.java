package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.AuthCommand;
import com.balsamic.sejongmalsami.object.AuthDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.RefreshToken;
import com.balsamic.sejongmalsami.repository.mongo.RefreshTokenRepository;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
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
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtUtil jwtUtil;

  /**
   * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
   *
   */
  @Transactional(readOnly = true)
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
      refreshTokenRepository.delete(storedToken); // 만료된 토큰 삭제
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰에서 사용자 정보 추출
    Claims claims = jwtUtil.getClaims(refreshToken);
    String username = claims.getSubject();

    // 사용자 정보 로드
    CustomUserDetails userDetails = memberService.loadUserByUsername(username);

    // 새로운 액세스 토큰 생성
    String newAccessToken = jwtUtil.createAccessToken(userDetails);

    log.info("새로운 AccessToken 발급 완료: 회원 = {}", userDetails.getMember().getStudentId());

    return AuthDto.builder()
        .accessToken(newAccessToken)
        .build();
  }
}
