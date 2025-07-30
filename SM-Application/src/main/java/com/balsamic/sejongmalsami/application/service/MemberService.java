package com.balsamic.sejongmalsami.application.service;

import com.balsamic.sejongmalsami.application.dto.AdminCommand;
import com.balsamic.sejongmalsami.application.dto.AdminDto;
import com.balsamic.sejongmalsami.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.member.dto.MemberCommand;
import com.balsamic.sejongmalsami.member.dto.MemberDto;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.post.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.post.object.postgres.Comment;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.post.repository.mongo.CommentLikeRepository;
import com.balsamic.sejongmalsami.post.repository.mongo.DocumentBoardLikeRepository;
import com.balsamic.sejongmalsami.post.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.ExpRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.service.ExpService;
import com.balsamic.sejongmalsami.service.YeopjeonService;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  private final ExpService expService;
  private final YeopjeonService yeopjeonService;

  private final MemberRepository memberRepository;
  private final CommentRepository commentRepository;
  private final ExpRepository expRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final DocumentBoardLikeRepository documentBoardLikeRepository;
  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final CommentLikeRepository commentLikeRepository;

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
  public AdminDto getAllMembers(AdminCommand command) {

    Sort sort = Sort.by(
        command.getSortDirection().equalsIgnoreCase("desc") ?
            Sort.Direction.DESC : Sort.Direction.ASC,
        command.getSortField()
    );

    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    return AdminDto.builder()
        .membersPage(memberRepository.findAll(pageable))
        .build();
  }


}
