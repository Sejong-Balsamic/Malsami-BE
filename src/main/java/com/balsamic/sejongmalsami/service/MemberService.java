package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.mongo.RefreshToken;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.CommentLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.DocumentBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.RefreshTokenRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.ExpRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.util.config.AdminConfig;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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

  private final YeopjeonConfig yeopjeonConfig;
  private final AdminConfig adminConfig;

  private final JwtUtil jwtUtil;
  private final SejongPortalAuthenticator sejongPortalAuthenticator;

  private final ExpService expService;
  private final YeopjeonService yeopjeonService;

  private final MemberRepository memberRepository;
  private final CommentRepository commentRepository;
  private final ExpRepository expRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final DocumentBoardLikeRepository documentBoardLikeRepository;
  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final CommentLikeRepository commentLikeRepository;



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

    boolean isFirstLogin = false;
    boolean isAdmin = false;
    Yeopjeon yeopjeon = null;

    // 인증 정보 조회
    MemberDto dto = sejongPortalAuthenticator.getMemberAuthInfos(command);
    String studentIdString = dto.getStudentIdString();
    Long studentId = Long.parseLong(studentIdString);

    // 회원 조회 또는 신규 등록
    Member member = memberRepository.findByStudentId(studentId)
        .orElseGet(() -> {

          // 관리자 계정 확인
          HashSet<Role> roles = new HashSet<>(Set.of(Role.ROLE_USER));
          if(adminConfig.isAdmin(studentIdString)){
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
                  .build());

          log.info("신규 회원 : Exp 객체 생성 : {}", exp.getExpId());

          return newMember;
        });

    // 관리자 확인
    if(member.getRoles().contains(Role.ROLE_ADMIN)){
      isAdmin = true;
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
    member.updateLastLoginTime(LocalDateTime.now());
    log.info("회원 로그인 완료: studentId = {} , memberId = {}", studentId, member.getMemberId());
    memberRepository.save(member);

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


  @Transactional(readOnly = true)
  public MemberDto myPage(MemberCommand command) {
    // 회원
    Member member = command.getMember();
    log.info("회원 마이페이지 정보: {}", member);

    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
    log.info("엽전 정보: {}", yeopjeon);

    Exp exp = expRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND));
    log.info("경험치 정보: {}", exp);

    // 랭킹 관련 변수
    int yeopjeonRank = yeopjeonService.getYeopjeonRank(member);
    int totalYeopjeonCount = yeopjeonService.getTotalYeopjeonCount();
    double yeopjeonPercentile = FileUtil.calculatePercentile(totalYeopjeonCount, yeopjeonRank);
    log.info("엽전 랭킹: {}, 총 엽전 수: {}, Percentile: {}", yeopjeonRank, totalYeopjeonCount, yeopjeonPercentile);

    int expRank = expService.getExpRank(member);
    int totalExpCount = expService.getTotalExpCount();
    double expPercentile = FileUtil.calculatePercentile(totalExpCount, expRank);
    log.info("경험치 랭킹: {}, 총 경험치 수: {}, Percentile: {}", expRank, totalExpCount, expPercentile);

    // 게시글 및 댓글 관련 변수
    long totalComment = commentRepository.countByMember(member);
    long questionPostCount = questionPostRepository.countByMember(member);
    long answerPostCount = answerPostRepository.countByMember(member);
    long documentPostCount = documentPostRepository.countByMember(member);
    long documentRequestPostCount = documentRequestPostRepository.countByMember(member);
    long totalPost = questionPostCount + answerPostCount + documentPostCount + documentRequestPostCount;
    long totalPopularPost = documentPostRepository.countByMemberAndIsPopularTrue(member);
    log.info("댓글 수: {}, 총 게시글 수: {}, 인기자료 수: {}", totalComment, totalPost, totalPopularPost);

    // 좋아요
    long totalLike = 0;

    // 질문 좋아요 수
    List<QuestionPost> questionPosts = questionPostRepository.findByMember(member);
    List<UUID> questionPostIds = questionPosts.stream()
        .map(QuestionPost::getQuestionPostId)
        .collect(Collectors.toList());
    long questionLikeCount = questionBoardLikeRepository.countByQuestionBoardIdIn(questionPostIds);
    log.info("질문 게시글 좋아요 수: {}", questionLikeCount);
    totalLike += questionLikeCount;

    // 답변 좋아요 수
    List<AnswerPost> answerPosts = answerPostRepository.findByMember(member);
    List<UUID> answerPostIds = answerPosts.stream()
        .map(AnswerPost::getAnswerPostId)
        .collect(Collectors.toList());
    long answerLikeCount = questionBoardLikeRepository.countByQuestionBoardIdIn(answerPostIds);
    log.info("답변 게시글 좋아요 수: {}", answerLikeCount);
    totalLike += answerLikeCount;

    // 자료글 좋아요 수
    List<DocumentPost> documentPosts = documentPostRepository.findByMember(member);
    List<UUID> documentPostIds = documentPosts.stream()
        .map(DocumentPost::getDocumentPostId)
        .collect(Collectors.toList());
    long documentLikeCount = documentBoardLikeRepository.countByDocumentBoardIdIn(documentPostIds);
    log.info("문서 게시글 좋아요 수: {}", documentLikeCount);
    totalLike += documentLikeCount;

    // 자료요청글 좋아요 수
    List<DocumentRequestPost> documentRequestPosts = documentRequestPostRepository.findByMember(member);
    List<UUID> documentRequestPostIds = documentRequestPosts.stream()
        .map(DocumentRequestPost::getDocumentRequestPostId)
        .collect(Collectors.toList());
    long documentRequestLikeCount = documentBoardLikeRepository.countByDocumentBoardIdIn(documentRequestPostIds);
    log.info("문서 요청 게시글 좋아요 수: {}", documentRequestLikeCount);
    totalLike += documentRequestLikeCount;

    // 댓글 좋아요 수
    List<Comment> comments = commentRepository.findByMember(member);
    List<UUID> commentIds = comments.stream()
        .map(Comment::getCommentId)
        .collect(Collectors.toList());
    long commentLikeCount = commentLikeRepository.countByCommentIdIn(commentIds);
    log.info("댓글 좋아요 수: {}", commentLikeCount);
    totalLike += commentLikeCount;

    // 총 좋아요
    log.info("총 좋아요 수: {}", totalLike);

    return MemberDto.builder()
        .member(member)
        .yeopjeon(yeopjeon)
        .yeopjeonRank(yeopjeonRank)
        .totalYeopjeonCount(totalYeopjeonCount)
        .yeopjeonPercentile(yeopjeonPercentile)
        .exp(exp)
        .expRank(expRank)
        .totalExp(totalExpCount)
        .expPercentile(expPercentile)
        .totalComment(totalComment)
        .totalPost(totalPost)
        .totalLike(totalLike)
        .totalPopularPost(totalPopularPost)
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
