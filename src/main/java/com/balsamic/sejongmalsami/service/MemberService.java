package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.mongo.RefreshToken;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.RefreshTokenRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {

  private final MemberRepository memberRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final SejongPortalAuthenticator sejongPortalAuthenticator;
  private final JwtUtil jwtUtil;
  private final YeopjeonConfig yeopjeonConfig;

  /**
   * Spring Security에서 회원 정보를 로드하는 메서드
   */
  @Override
  public CustomUserDetails loadUserByUsername(String stringMemberId) throws UsernameNotFoundException {
    UUID memberId;
    try {
      memberId = UUID.fromString(stringMemberId);
    } catch (IllegalArgumentException e) {
      log.error("유효하지 않은 UUID 형식: {}", stringMemberId);
      throw new UsernameNotFoundException("유효하지 않은 UUID 형식입니다.");
    }

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원 미발견: {}", memberId);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
    return new CustomUserDetails(member);
  }

  /**
   * 회원 로그인 처리
   */
  @Transactional
  public MemberDto signIn(MemberCommand command, HttpServletResponse response) {
    // 인증 정보 조회
    MemberDto dto = sejongPortalAuthenticator.getMemberAuthInfos(command);
    Long studentId = Long.parseLong(dto.getStudentIdString());

    // 회원 조회 또는 신규 등록
    Member member = memberRepository.findByStudentId(studentId)
        .orElseGet(() -> {
          log.info("신규 회원 등록: studentId = {}", studentId);
          return Member.builder()
              .studentId(studentId)
              .studentName(dto.getStudentName())
              .uuidNickname(UUID.randomUUID().toString().substring(0, 6))
              .major(dto.getMajor())
              .academicYear(dto.getAcademicYear())
              .enrollmentStatus(dto.getEnrollmentStatus())
              .isNotificationEnabled(true)
              .role(Role.ROLE_USER)
              .accountStatus(AccountStatus.ACTIVE)
              .isFirstLogin(true)
              .build();
        });

    // 첫 로그인 여부 확인
    if (memberRepository.existsByStudentId(studentId)) {
      member.disableFirstLogin();
    }

    // 마지막 로그인 시간 업데이트
    member.updateLastLoginTime(LocalDateTime.now());
    log.info("회원 로그인 완료: studentId = {}", studentId);
    memberRepository.save(member);

    // 첫 로그인 시 엽전 테이블 생성
    if (member.getIsFirstLogin()) {
      yeopjeonRepository.save(Yeopjeon.builder()
          .member(member)
          .resultYeopjeon(yeopjeonConfig.getCreateAccount()) // 첫 로그인 보상
          .build());
    }

    // 회원 상세 정보 로드
    CustomUserDetails userDetails = new CustomUserDetails(member);

    // 액세스 토큰 및 리프레시 토큰 생성
    String accessToken = jwtUtil.createAccessToken(userDetails);
    String refreshToken = jwtUtil.createRefreshToken(userDetails);
    log.info("액세스 토큰 및 리프레시 토큰 생성 완료: 회원 = {}", member.getStudentId());
    log.info("accessToken = {}", accessToken);
    log.info("refreshToken = {}", refreshToken);

    // Refresh Token 저장
    RefreshToken refreshTokenEntity = RefreshToken.builder()
        .token(refreshToken)
        .memberId(member.getMemberId())
        .expiryDate(jwtUtil.getRefreshExpiryDate())
        .build();
    refreshTokenRepository.save(refreshTokenEntity);
    log.info("리프레시 토큰 저장 완료: 회원 = {}", member.getStudentId());

    // Refresh Token : HTTP-Only 쿠키 설정
    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(false);   //FIXME: 개발 환경에서는 false, 프로덕션에서는 true
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtUtil.getRefreshExpirationTime() / 1000)); // 7일
    // SameSite 설정은 직접 Set-Cookie 헤더에 추가

    // 쿠키 설정 정보 로깅
    log.info("설정할 쿠키 정보: ");
    log.info("Name: {}", refreshCookie.getName());
    log.info("Value: {}", refreshCookie.getValue());
    log.info("HttpOnly: {}", refreshCookie.isHttpOnly());
    log.info("Secure: {}", refreshCookie.getSecure());
    log.info("Path: {}", refreshCookie.getPath());
    log.info("Max-Age: {}", refreshCookie.getMaxAge());

    // 쿠키에 SameSite 속성 추가
    StringBuilder cookieBuilder = new StringBuilder();
    cookieBuilder.append(refreshCookie.getName()).append("=").append(refreshCookie.getValue()).append(";");
    cookieBuilder.append(" Path=").append(refreshCookie.getPath()).append(";");
    cookieBuilder.append(" Max-Age=").append(refreshCookie.getMaxAge()).append(";");
    cookieBuilder.append(" SameSite=None;"); //FIXME: 모든 요청에서 쿠키 전송
    cookieBuilder.append(" Secure;"); //FIXME: Secure 속성 설정

    if (refreshCookie.isHttpOnly()) {
      cookieBuilder.append(" HttpOnly;");
    }

    String setCookieHeader = cookieBuilder.toString();
    response.addHeader("Set-Cookie", setCookieHeader);

    log.info("Set-Cookie Header: {}", setCookieHeader);

    log.info("리프레시 토큰 쿠키 설정 완료: 회원 = {}", member.getStudentId());

    // 액세스 토큰 반환
    return MemberDto.builder()
        .member(member)
        .accessToken(accessToken)
        .build();
  }

  /**
   * JWT 토큰에서 Authentication 객체 생성
   *
   * @param token JWT 토큰
   * @return Authentication 객체
   */
  public Authentication getAuthentication(String token) {
    Claims claims = jwtUtil.getClaims(token);
    String username = claims.getSubject();
    CustomUserDetails userDetails = loadUserByUsername(username);
    return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
  }
}
