package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.repository.MemberRepository;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
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
  private final SejongPortalAuthenticator sejongPortalAuthenticator;
  private final JwtUtil jwtUtil;

  /**
   * Spring Security에서 사용자 정보를 로드하는 메서드
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
  public MemberDto signIn(MemberCommand command) {
    MemberDto dto = sejongPortalAuthenticator.getMemberAuthInfos(command);
    Long studentId = Long.parseLong(dto.getStudentIdString());

    Member member = memberRepository.findByStudentId(studentId)
        .orElseGet(() -> {
          log.info("신규 회원 등록: studentId = {}", studentId);
          return memberRepository.save(
              Member.builder()
                  .studentId(studentId)
                  .studentName(dto.getStudentName())
                  .uuidNickname(UUID.randomUUID().toString().substring(0, 6))
                  .major(dto.getMajor())
                  .academicYear(dto.getAcademicYear())
                  .enrollmentStatus(dto.getEnrollmentStatus())
                  .lastLoginTime(LocalDateTime.now())
                  .build());
        });

    member.setLastLoginTime(LocalDateTime.now());
    log.info("회원 로그인 완료: studentId = {}", studentId);
    memberRepository.save(member);

    CustomUserDetails userDetails = new CustomUserDetails(member);
    String accessToken = jwtUtil.createAccessToken(userDetails);
    String refreshToken = jwtUtil.createRefreshToken(userDetails);
    log.info("액세스 토큰 및 리프레시 토큰 생성 완료: 사용자 = {}", userDetails.getUsername());
    log.info("accessToken = {}", accessToken);
    log.info("refreshToken = {}", refreshToken);

    return MemberDto.builder()
        .member(member)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
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
