package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.ContentType.ANSWER;
import static com.balsamic.sejongmalsami.object.constants.ContentType.DOCUMENT;
import static com.balsamic.sejongmalsami.object.constants.ContentType.DOCUMENT_REQUEST;
import static com.balsamic.sejongmalsami.object.constants.ContentType.QUESTION;
import static com.balsamic.sejongmalsami.object.constants.SortType.LATEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.MOST_LIKED;
import static com.balsamic.sejongmalsami.object.constants.SortType.OLDEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.VIEW_COUNT;
import static com.balsamic.sejongmalsami.object.constants.SortType.getJpqlSortOrder;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.ExpTier;
import com.balsamic.sejongmalsami.object.constants.FileStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.mongo.RefreshToken;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.Department;
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
import com.balsamic.sejongmalsami.repository.postgres.DepartmentRepository;
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
import jakarta.servlet.http.Cookie;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

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
  private final DepartmentRepository departmentRepository;

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

    // 엽전
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
    log.info("엽전 정보: {}", yeopjeon);

    // 엽전 : 랭킹 계산
    int yeopjeonRank = yeopjeonService.getYeopjeonRank(member);
    int totalYeopjeonMembers = yeopjeonService.getCountOfMembersWithYeopjeon();
    double yeopjeonPercentile = FileUtil.calculatePercentile(totalYeopjeonMembers, yeopjeonRank);
    log.info("엽전 랭킹: {}, 총 엽전을 가진 사람수: {}, Percentile: {}", yeopjeonRank, totalYeopjeonMembers, yeopjeonPercentile);

    // 게시판 접근 권한 정보
    boolean canAccessCheonmin = true;
    boolean canAccessJungin = yeopjeon.getYeopjeon() >= yeopjeonConfig.getJunginRequirement();
    boolean canAccessYangban = yeopjeon.getYeopjeon() >= yeopjeonConfig.getYangbanRequirement();
    boolean canAccessKing = yeopjeon.getYeopjeon() >= yeopjeonConfig.getKingRequirement();

    // 경험치
    Exp exp = expRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND));
    log.info("경험치 정보: {}", exp);

    int expRank = expService.getExpRank(member);
    int totalExpMembers = expService.getCountOfMembersWithExp();
    double expPercentile = FileUtil.calculatePercentile(totalExpMembers, expRank);
    log.info("경험치 랭킹: {}, 총 경험치를 가진 사람수: {}, Percentile: {}", expRank, totalExpMembers, expPercentile);

    // 게시글 및 댓글
    long questionPostCount = questionPostRepository.countByMember(member);
    long answerPostCount = answerPostRepository.countByMember(member);
    long documentPostCount = documentPostRepository.countByMember(member);
    long documentRequestPostCount = documentRequestPostRepository.countByMember(member);
    long totalCommentCount = commentRepository.countByMember(member);
    long totalPostCount = questionPostCount + answerPostCount + documentPostCount + documentRequestPostCount;
    long totalPopularPostCount = documentPostRepository.countByMemberAndIsPopularTrue(member);
    log.info("댓글 수: {}, 총 게시글 수: {}, 인기자료 수: {}", totalCommentCount, totalPostCount, totalPopularPostCount);

    // 좋아요
    long totalLikeCount = 0;

    // 질문 좋아요 수
    List<QuestionPost> questionPosts = questionPostRepository.findByMember(member);
    List<UUID> questionPostIds = questionPosts.stream()
        .map(QuestionPost::getQuestionPostId)
        .collect(Collectors.toList());
    long questionLikeCount = questionBoardLikeRepository.countByQuestionBoardIdIn(questionPostIds);
    log.info("질문 게시글 좋아요 수: {}", questionLikeCount);
    totalLikeCount += questionLikeCount;

    // 답변 좋아요 수
    List<AnswerPost> answerPosts = answerPostRepository.findByMember(member);
    List<UUID> answerPostIds = answerPosts.stream()
        .map(AnswerPost::getAnswerPostId)
        .collect(Collectors.toList());
    long answerLikeCount = questionBoardLikeRepository.countByQuestionBoardIdIn(answerPostIds);
    log.info("답변 게시글 좋아요 수: {}", answerLikeCount);
    totalLikeCount += answerLikeCount;

    // 자료글 좋아요 수
    List<DocumentPost> documentPosts = documentPostRepository.findByMember(member);
    List<UUID> documentPostIds = documentPosts.stream()
        .map(DocumentPost::getDocumentPostId)
        .collect(Collectors.toList());
    long documentLikeCount = documentBoardLikeRepository.countByDocumentBoardIdIn(documentPostIds);
    log.info("문서 게시글 좋아요 수: {}", documentLikeCount);
    totalLikeCount += documentLikeCount;

    // 자료요청글 좋아요 수
    List<DocumentRequestPost> documentRequestPosts = documentRequestPostRepository.findByMember(member);
    List<UUID> documentRequestPostIds = documentRequestPosts.stream()
        .map(DocumentRequestPost::getDocumentRequestPostId)
        .collect(Collectors.toList());
    long documentRequestLikeCount = documentBoardLikeRepository.countByDocumentBoardIdIn(documentRequestPostIds);
    log.info("문서 요청 게시글 좋아요 수: {}", documentRequestLikeCount);
    totalLikeCount += documentRequestLikeCount;

    // 댓글 좋아요 수
    List<Comment> comments = commentRepository.findByMember(member);
    List<UUID> commentIds = comments.stream()
        .map(Comment::getCommentId)
        .collect(Collectors.toList());
    long commentLikeCount = commentLikeRepository.countByCommentIdIn(commentIds);
    log.info("댓글 좋아요 수: {}", commentLikeCount);
    totalLikeCount += commentLikeCount;

    // 총 좋아요
    log.info("총 좋아요 수: {}", totalLikeCount);

    return MemberDto.builder()
        .member(member)                                     // 회원 정보
        .yeopjeon(yeopjeon)                                 // 엽전 정보
        .yeopjeonRank(yeopjeonRank)                         // 엽전 랭킹
        .totalYeopjeonMembers(totalYeopjeonMembers)         // 총 (엽전을가진) 사람수
        .yeopjeonPercentile(yeopjeonPercentile)             // 엽전 백분위
        .exp(exp)                                           // 경험치 정보
        .expRank(expRank)                                   // 경험치 랭킹
        .totalExpMembers(totalExpMembers)                   // 총 (경험치를가진) 사람수
        .expPercentile(expPercentile)                       // 경험치 백분위
        .questionPostCount(questionPostCount)               // 질문 게시글 수
        .answerPostCount(answerPostCount)                   // 답변 게시글 수
        .documentPostCount(documentPostCount)               // 문서 게시글 수
        .documentRequestPostCount(documentRequestPostCount) // 문서 요청 게시글 수
        .totalPostCount(totalPostCount)                     // 총 게시글 수
        .totalCommentCount(totalCommentCount)               // 총 댓글 수
        .totalPopularPostCount(totalPopularPostCount)       // 총 인기자료 수
        .totalLikeCount(totalLikeCount)                     // 총 좋아요 수
        .cheonminRequirement(yeopjeonConfig.getCheonminRequirement())
        .junginRequirement(yeopjeonConfig.getJunginRequirement())
        .yangbanRequirement(yeopjeonConfig.getYangbanRequirement())
        .kingRequirement(yeopjeonConfig.getKingRequirement())
        .canAccessCheonmin(canAccessCheonmin)
        .canAccessJungin(canAccessJungin)
        .canAccessYangban(canAccessYangban)
        .canAccessKing(canAccessKing)
        .build();
  }

  /**
   * 회원의 yeopjeon -> 각 자료게시판 접근 가능 여부 반환
   */
  public MemberDto getDocumentBoardAccessByTier(MemberCommand command) {
    Member member = command.getMember();

    // 엽전 정보
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);

    // 게시판 정보
    boolean canAccessCheonmin = true; // 천민 게시판은 모든 회원이 접근 가능
    boolean canAccessJungin = yeopjeon.getYeopjeon() >= yeopjeonConfig.getJunginRequirement();
    boolean canAccessYangban = yeopjeon.getYeopjeon() >= yeopjeonConfig.getYangbanRequirement();
    boolean canAccessKing = yeopjeon.getYeopjeon() >= yeopjeonConfig.getKingRequirement();

    return MemberDto.builder()
        .cheonminRequirement(yeopjeonConfig.getCheonminRequirement())
        .junginRequirement(yeopjeonConfig.getJunginRequirement())
        .yangbanRequirement(yeopjeonConfig.getYangbanRequirement())
        .kingRequirement(yeopjeonConfig.getKingRequirement())
        .canAccessCheonmin(canAccessCheonmin)
        .canAccessJungin(canAccessJungin)
        .canAccessYangban(canAccessYangban)
        .canAccessKing(canAccessKing)
        .yeopjeon(yeopjeon)
        .build();
  }

  // 관리자-회원관리 : 기본 검색 (미사용)
  public MemberDto getAllMembers(MemberCommand command) {

    Sort sort = Sort.by(
        command.getSortDirection().equalsIgnoreCase("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC,
        command.getSortField()
    );

    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    return MemberDto.builder()
        .membersPage(memberRepository.findAll(pageable))
        .build();
  }

  /**
   * <h3>특정 사용자가 작성한 글 조회 로직</h3>
   * <p>AccessToken을 통해 사용자를 확인합니다.</p>
   * <p>contentType에 따른 글을 반환합니다.</p>
   * <p>sortType에 해당하는 정렬조건으로 글을 반환합니다.</p>
   *
   * @param command member, contentType, sortType, pageNumber, pageSize
   * @return MemberDto
   */
  public MemberDto getAllMemberPost(MemberCommand command) {

    // 정렬 기준 (default: 최신순)
    SortType sortType = (command.getSortType() != null) ? command.getSortType() : LATEST;
    if (!sortType.equals(LATEST) &&
        !sortType.equals(OLDEST) &&
        !sortType.equals(MOST_LIKED) &&
        !sortType.equals(VIEW_COUNT)) {
      log.error("잘못된 sortType 요청입니다. 요청된 sortType: {}", command.getSortType());
      throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
    }

    Sort sort = getJpqlSortOrder(sortType);

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort);

    Page<QuestionPost> questionPostPage = null;
    Page<DocumentPost> documentPostPage = null;
    Page<DocumentRequestPost> documentRequestPostPage = null;

    if (command.getContentType().equals(QUESTION)) { // 내가 작성한 질문글
      questionPostPage = questionPostRepository
          .findAllByMember(command.getMember(), pageable);
    } else if (command.getContentType().equals(ANSWER)) { // 내가 답변을 작성한 글
      questionPostPage = questionPostRepository
          .findAllByAnsweredByMember(command.getMember(), pageable);
    } else if (command.getContentType().equals(DOCUMENT)) { // 내가 작성한 자료글
      documentPostPage = documentPostRepository
          .findAllByMember(command.getMember(), pageable);
    } else if (command.getContentType().equals(DOCUMENT_REQUEST)) { // 내가 작성한 자료요청글
      documentRequestPostPage = documentRequestPostRepository
          .findAllByMember(command.getMember(), pageable);
    } else {
      log.error("잘못된 contentType 입니다. 요청된 contentType: {}", command.getContentType());
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    return MemberDto.builder()
        .questionPostsPage(questionPostPage)
        .documentPostsPage(documentPostPage)
        .documentRequestPostsPage(documentRequestPostPage)
        .build();
  }
}
