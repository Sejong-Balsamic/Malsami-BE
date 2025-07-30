package com.balsamic.sejongmalsami.application.service;

import com.balsamic.sejongmalsami.application.JwtUtil;
import com.balsamic.sejongmalsami.auth.dto.AuthCommand;
import com.balsamic.sejongmalsami.auth.dto.AuthDto;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.auth.dto.WebLoginDto;
import com.balsamic.sejongmalsami.auth.object.mongo.RefreshToken;
import com.balsamic.sejongmalsami.auth.repository.mongo.RefreshTokenRepository;
import com.balsamic.sejongmalsami.auth.service.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.config.AdminConfig;
import com.balsamic.sejongmalsami.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.constants.AccountStatus;
import com.balsamic.sejongmalsami.constants.ExpTier;
import com.balsamic.sejongmalsami.constants.FileStatus;
import com.balsamic.sejongmalsami.constants.Role;
import com.balsamic.sejongmalsami.member.dto.MemberDto;
import com.balsamic.sejongmalsami.object.postgres.Department;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.DepartmentRepository;
import com.balsamic.sejongmalsami.repository.postgres.ExpRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService {

  private final AdminConfig adminConfig;
  private final YeopjeonConfig yeopjeonConfig;
  // private final com.balsamic.sejongmalsami.web.service.MemberService memberService; // 순환의존성 방지를 위해 제거
  private final com.balsamic.sejongmalsami.auth.service.CustomUserDetailsService customUserDetailsService;
  private final com.balsamic.sejongmalsami.auth.service.FcmTokenService fcmTokenService;
  private final JwtUtil jwtUtil;
  private final SejongPortalAuthenticator sejongPortalAuthenticator;
  private final RefreshTokenRepository refreshTokenRepository;
  private final MemberRepository memberRepository;
  private final ExpRepository expRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final DepartmentRepository departmentRepository;

  /**
   * 회원 로그인 처리
   */
  @Transactional
  public MemberDto signIn(AuthCommand command, HttpServletResponse response) {

    boolean isFirstLogin = false;
    boolean isAdmin = false;
    Yeopjeon yeopjeon = null;

    // 인증 정보 조회
    AuthDto dto = sejongPortalAuthenticator.getMemberAuthInfos(command);
    String studentIdString = dto.getStudentIdString();
    Long studentId = Long.parseLong(studentIdString);

    // 회원 조회 또는 신규 등록
    Member member = memberRepository.findByStudentId(studentId)
        .orElseGet(() -> {

          // 관리자 계정 확인
          HashSet<Role> roles = new HashSet<>(Set.of(Role.ROLE_USER));
          if (adminConfig.isAdmin(studentIdString)) {
            roles = new HashSet<>(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));
            log.info("관리자 계정 등록 완료: {}", studentIdString);
          }

          log.info("신규 회원 등록: studentId = {}", studentId);
          Member newMember = memberRepository.save(
              Member.builder()
                  .studentId(studentId)
                  .studentName(dto.getStudentName())
                  .uuidNickname(UUID.randomUUID().toString().substring(0, 6))
                  .major(dto.getMajor())
                  .academicYear(dto.getAcademicYear())
                  .enrollmentStatus(dto.getEnrollmentStatus())
                  .isNotificationEnabled(true)
                  .roles(roles)
                  .accountStatus(AccountStatus.ACTIVE)
                  .isFirstLogin(true)
                  .build());

          // Exp 엔티티 생성 및 저장
          Exp exp = expRepository.save(
              Exp.builder()
                  .member(newMember)
                  .exp(0)
                  .expTier(ExpTier.R)
                  .tierStartExp(0)
                  .tierEndExp(500)
                  .progressPercent(0.0)
                  .build());

          log.info("신규 회원 : Exp 객체 생성 : {}", exp.getExpId());

          return newMember;
        });

    // 관리자 확인
    if (member.getRoles().contains(Role.ROLE_ADMIN)) {
      isAdmin = true;
    }

    // Faculty 설정
    String major = member.getMajor();
    Optional<List<Department>> departments = departmentRepository.findDeptMPrintOrDeptSPrint(major, major);

    if (departments.isPresent() && !departments.get().isEmpty()) {
      List<String> facultyNames = departments.get().stream()
          .map(dept -> dept.getFaculty().getName())
          .distinct()
          .collect(Collectors.toList());
      member.setFaculties(facultyNames);
      log.info("Faculties 설정 완료: {} -> {}", member.getMemberId(), facultyNames);
    } else {
      member.setFaculties(Collections.singletonList(FileStatus.NOT_FOUND.name()));
      log.warn("Member의 major에 해당하는 Department를 찾을 수 없습니다: {}", major);
    }

    // 첫 로그인 여부 확인
    if (member.getIsFirstLogin()) {
      isFirstLogin = true;

      // 엽전 보상 지급
      //TODO: 엽전 이력 관리 로직을 포함한 메소드 정의
      yeopjeon = yeopjeonRepository.save(Yeopjeon.builder()
          .member(member)
          .yeopjeon(yeopjeonConfig.getCreateAccount()) // 첫 로그인 보상
          .build());
      log.info("첫 로그인 엽전 보상 지급: Yeopjeon ID = {}", yeopjeon.getYeopjeonId());

      // 첫 로그인 플래그 비활성화
      member.disableFirstLogin();
    }

    // 마지막 로그인 시간 업데이트
    member.setLastLoginTime(LocalDateTime.now());
    log.info("회원 로그인 완료: studentId = {} , memberId = {}", studentId, member.getMemberId());
    memberRepository.save(member);

    // 회원 상세 정보 로드
    CustomUserDetails userDetails = new CustomUserDetails(member);

    // 액세스 토큰 및 리프레시 토큰 생성
    String accessToken = jwtUtil.createAccessToken(userDetails.getUsername());
    String refreshToken = jwtUtil.createRefreshToken(userDetails.getUsername());
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

//    //FIXME: 임시 로깅: 쿠키 설정
//    log.info("설정할 쿠키 정보: ");
//    log.info("Name: {}", refreshCookie.getName());
//    log.info("Value: {}", refreshCookie.getValue());
//    log.info("HttpOnly: {}", refreshCookie.isHttpOnly());
//    log.info("Secure: {}", refreshCookie.getSecure());
//    log.info("Path: {}", refreshCookie.getPath());
//    log.info("Max-Age: {}", refreshCookie.getMaxAge());

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
        .isFirstLogin(isFirstLogin)
        .isAdmin(isAdmin)
        .yeopjeon(yeopjeon)
        .exp(expRepository.findByMember(member)
            .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND)))
        .build();
  }

  /**
   * 모바일 전용 회원 로그인 처리 (쿠키 사용 안함)
   */
  @Transactional
  public MemberDto signInForMobile(AuthCommand command) {

    boolean isFirstLogin = false;
    boolean isAdmin = false;
    Yeopjeon yeopjeon = null;

    // 인증 정보 조회
    AuthDto dto = sejongPortalAuthenticator.getMemberAuthInfos(command);
    String studentIdString = dto.getStudentIdString();
    Long studentId = Long.parseLong(studentIdString);

    // 회원 조회 또는 신규 등록
    Member member = memberRepository.findByStudentId(studentId)
        .orElseGet(() -> {

          // 관리자 계정 확인
          HashSet<Role> roles = new HashSet<>(Set.of(Role.ROLE_USER));
          if (adminConfig.isAdmin(studentIdString)) {
            roles = new HashSet<>(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));
            log.info("모바일: 관리자 계정 등록 완료: {}", studentIdString);
          }

          log.info("모바일: 신규 회원 등록: studentId = {}", studentId);
          Member newMember = memberRepository.save(
              Member.builder()
                  .studentId(studentId)
                  .studentName(dto.getStudentName())
                  .uuidNickname(UUID.randomUUID().toString().substring(0, 6))
                  .major(dto.getMajor())
                  .academicYear(dto.getAcademicYear())
                  .enrollmentStatus(dto.getEnrollmentStatus())
                  .isNotificationEnabled(true)
                  .roles(roles)
                  .accountStatus(AccountStatus.ACTIVE)
                  .isFirstLogin(true)
                  .build());

          // Exp 엔티티 생성 및 저장
          Exp exp = expRepository.save(
              Exp.builder()
                  .member(newMember)
                  .exp(0)
                  .expTier(ExpTier.R)
                  .tierStartExp(0)
                  .tierEndExp(500)
                  .progressPercent(0.0)
                  .build());

          log.info("모바일: 신규 회원 : Exp 객체 생성 : {}", exp.getExpId());

          return newMember;
        });

    // 관리자 확인
    if (member.getRoles().contains(Role.ROLE_ADMIN)) {
      isAdmin = true;
    }

    // Faculty 설정
    String major = member.getMajor();
    Optional<List<Department>> departments = departmentRepository.findDeptMPrintOrDeptSPrint(major, major);

    if (departments.isPresent() && !departments.get().isEmpty()) {
      List<String> facultyNames = departments.get().stream()
          .map(dept -> dept.getFaculty().getName())
          .distinct()
          .collect(Collectors.toList());
      member.setFaculties(facultyNames);
      log.info("모바일: Faculties 설정 완료: {} -> {}", member.getMemberId(), facultyNames);
    } else {
      member.setFaculties(Collections.singletonList(FileStatus.NOT_FOUND.name()));
      log.warn("모바일: Member의 major에 해당하는 Department를 찾을 수 없습니다: {}", major);
    }

    // 첫 로그인 여부 확인
    if (member.getIsFirstLogin()) {
      isFirstLogin = true;

      // 엽전 보상 지급
      yeopjeon = yeopjeonRepository.save(Yeopjeon.builder()
          .member(member)
          .yeopjeon(yeopjeonConfig.getCreateAccount()) // 첫 로그인 보상
          .build());
      log.info("모바일: 첫 로그인 엽전 보상 지급: Yeopjeon ID = {}", yeopjeon.getYeopjeonId());

      // 첫 로그인 플래그 비활성화
      member.disableFirstLogin();
    }

    // 마지막 로그인 시간 업데이트
    member.setLastLoginTime(LocalDateTime.now());
    log.info("모바일: 회원 로그인 완료: studentId = {} , memberId = {}", studentId, member.getMemberId());
    memberRepository.save(member);

    // 회원 상세 정보 로드
    CustomUserDetails userDetails = new CustomUserDetails(member);

    // 액세스 토큰 및 리프레시 토큰 생성
    String accessToken = jwtUtil.createAccessToken(userDetails.getUsername());
    String refreshToken = jwtUtil.createRefreshToken(userDetails.getUsername());

    log.info("모바일: 액세스 토큰 및 리프레시 토큰 생성 완료: 회원 = {}", member.getStudentId());
    log.info("모바일: accessToken = {}", accessToken);
    log.info("모바일: refreshToken = {}", refreshToken);

    // Refresh Token 저장 (쿠키 설정 없음)
    RefreshToken refreshTokenEntity = RefreshToken.builder()
        .token(refreshToken)
        .memberId(member.getMemberId())
        .expiryDate(jwtUtil.getRefreshExpiryDate())
        .build();
    refreshTokenRepository.save(refreshTokenEntity);
    log.info("모바일: 리프레시 토큰 저장 완료: 회원 = {}", member.getStudentId());

    // 액세스 토큰 및 리프레시 토큰 반환
    return MemberDto.builder()
        .member(member)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .isFirstLogin(isFirstLogin)
        .isAdmin(isAdmin)
        .yeopjeon(yeopjeon)
        .exp(expRepository.findByMember(member)
            .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND)))
        .build();
  }

  /**
   * 리프레시 토큰을 사용하여 새로운 액세스 토큰 발급
   */
  @Transactional
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
      refreshTokenRepository.deleteByToken(refreshToken); // 만료된 토큰 삭제
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰에서 사용자 정보 추출
    Claims claims = jwtUtil.getClaims(refreshToken);
    String username = claims.getSubject();

    // 사용자 정보 로드
    CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // 새로운 액세스 토큰 생성
    String newAccessToken = jwtUtil.createAccessToken(userDetails.getUsername());

    log.info("새로운 AccessToken 발급 완료: 회원 = {}", userDetails.getMember().getStudentId());
    log.info("새로운 AccessToken: {}", newAccessToken);

    return AuthDto.builder()
        .accessToken(newAccessToken)
        .studentName(userDetails.getMember().getStudentName()) //2024.11.24 : SUH : 학생 이름 반환 추가
        .memberId(userDetails.getMemberId()) //2024.11.27 : FE#166 : 키 추가 요청
        .build();
  }

  /**
   * 모바일 전용 토큰 갱신 (Access + Refresh 둘 다 재발급)
   */
  @Transactional
  public AuthDto refreshTokensForMobile(AuthCommand command) {
    String oldRefreshToken = command.getRefreshToken();
    
    // 리프레시 토큰 검증 (JWT 유효성 검사)
    if (!jwtUtil.validateToken(oldRefreshToken)) {
      log.error("모바일: 리프레시 토큰이 유효하지 않습니다.");
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰이 데이터베이스에 존재하는지 확인
    RefreshToken storedToken = refreshTokenRepository.findByToken(oldRefreshToken)
        .orElseThrow(() -> {
          log.error("모바일: 저장된 리프레시 토큰을 찾을 수 없습니다.");
          return new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        });

    // 리프레시 토큰 만료 여부 확인
    if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      log.error("모바일: 리프레시 토큰이 만료되었습니다.");
      refreshTokenRepository.deleteByToken(oldRefreshToken); // 만료된 토큰 삭제
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰에서 사용자 정보 추출
    Claims claims = jwtUtil.getClaims(oldRefreshToken);
    String username = claims.getSubject();

    // 사용자 정보 로드
    CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    // 새로운 액세스 토큰 및 리프레시 토큰 생성
    String newAccessToken = jwtUtil.createAccessToken(userDetails.getUsername());
    String newRefreshToken = jwtUtil.createRefreshToken(userDetails.getUsername());

    // 기존 리프레시 토큰 삭제
    refreshTokenRepository.deleteByToken(oldRefreshToken);
    log.info("모바일: 기존 리프레시 토큰 삭제 완료: 회원 = {}", userDetails.getMember().getStudentId());

    // 새로운 리프레시 토큰 저장
    RefreshToken newRefreshTokenEntity = RefreshToken.builder()
        .token(newRefreshToken)
        .memberId(userDetails.getMemberId())
        .expiryDate(jwtUtil.getRefreshExpiryDate())
        .build();
    refreshTokenRepository.save(newRefreshTokenEntity);
    log.info("모바일: 새로운 리프레시 토큰 저장 완료: 회원 = {}", userDetails.getMember().getStudentId());

    log.info("모바일: 새로운 AccessToken 및 RefreshToken 발급 완료: 회원 = {}", userDetails.getMember().getStudentId());

    return AuthDto.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .studentName(userDetails.getMember().getStudentName())
        .memberId(userDetails.getMemberId())
        .build();
  }

  /**
   * 관리자 페이지 로그인
   */
  public WebLoginDto webLogin(AuthCommand command, HttpServletResponse response) {
    log.info("관리자 로그인 시도: {}", command.getSejongPortalId());
    try {
      // 회원 로그인 사용
      MemberDto memberDto = this.signIn(command, response);
      log.info("로그인 결과: isAdmin={}, studentID={}", memberDto.getIsAdmin(), memberDto.getMember().getStudentId());

      // 관리자가 아닐시
      if(!memberDto.getIsAdmin()){
        log.warn("관리자 권한 없음: {}", command.getSejongPortalId());
        return WebLoginDto.builder()
            .success(false)
            .message("로그인에 실패했습니다. 관리자가 아닙니다")
            .build();
      }

      // 관리자인 경우
      log.info("관리자 로그인 성공: {}", command.getSejongPortalId());
      return WebLoginDto.builder()
          .success(true)
          .accessToken(memberDto.getAccessToken())
          .build();

    } catch (Exception e) {
      // 세종 로그인 실패
      log.error("로그인 실패: {}", command.getSejongPortalId(), e);
      return WebLoginDto.builder()
          .success(false)
          .message("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.")
          .build();
    }
  }

  /**
   * 로그아웃 처리
   */
  @Transactional
  public void logout(AuthCommand command, HttpServletRequest request, HttpServletResponse response) {
    // FCM 토큰 삭제
    fcmTokenService.deleteFcmToken(command);

    // 리프레시 토큰 추출
    String refreshToken = extractRefreshTokenFromCookies(request.getCookies());

    // 리프레시 토큰이 존재하는지 확인
    if (!refreshTokenRepository.findByToken(refreshToken).isPresent()) {
      log.error("삭제할 리프레시 토큰을 찾을 수 없습니다: {}", refreshToken);
      throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    // 리프레시 토큰 삭제
    refreshTokenRepository.deleteByToken(refreshToken);
    log.info("리프레시 토큰 삭제 완료: {}", refreshToken);

    // 리프레시 토큰 쿠키 삭제
    deleteRefreshTokenCookie(response);

    // AccessToken 삭제
    response.setHeader("Authorization", "Bearer ");
  }

  /**
   * 쿠키에서 리프레시 토큰 추출
   */
  private String extractRefreshTokenFromCookies(Cookie[] cookies) {
    if (cookies == null) {
      throw new CustomException(ErrorCode.MISSING_REFRESH_TOKEN);
    }
    for (Cookie cookie : cookies) {
      if ("refreshToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    throw new CustomException(ErrorCode.MISSING_REFRESH_TOKEN);
  }

  /**
   * 리프레시 토큰 쿠키 삭제
   */
  private void deleteRefreshTokenCookie(HttpServletResponse response) {
    Cookie deleteCookie = new Cookie("refreshToken", null);
    deleteCookie.setMaxAge(0);
    deleteCookie.setPath("/");
    deleteCookie.setHttpOnly(true);
    deleteCookie.setSecure(false);  //FIXME: 개발 환경 false, 프로덕션 true

    // 쿠키에 SameSite 속성 추가 : Cookie 객체에서 미지원하므로 수동으로 set-Cookie 헤더에 추가
    StringBuilder cookieBuilder = new StringBuilder();
    cookieBuilder.append(deleteCookie.getName()).append("=").append(deleteCookie.getValue()).append(";");
    cookieBuilder.append(" Path=").append(deleteCookie.getPath()).append(";");
    cookieBuilder.append(" Max-Age=0;"); // 쿠키 삭제
    cookieBuilder.append(" SameSite=None;"); //FIXME: 모든 요청에서 쿠키 전송
    cookieBuilder.append(" Secure;"); //FIXME: 개발 환경 false, 프로덕션 true

    if (deleteCookie.isHttpOnly()) {
      cookieBuilder.append(" HttpOnly;");
    }

    String setCookieHeader = cookieBuilder.toString();
    response.addHeader("Set-Cookie", setCookieHeader);

    log.info("로그아웃 : Set-Cookie Header (삭제): {}", setCookieHeader);
  }
}
