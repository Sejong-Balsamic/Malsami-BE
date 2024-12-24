package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.ContentType.ANSWER;
import static com.balsamic.sejongmalsami.object.constants.ContentType.COMMENT;
import static com.balsamic.sejongmalsami.object.constants.ContentType.DOCUMENT;
import static com.balsamic.sejongmalsami.object.constants.ContentType.DOCUMENT_REQUEST;
import static com.balsamic.sejongmalsami.object.constants.ContentType.QUESTION;
import static com.balsamic.sejongmalsami.object.constants.LikeType.DISLIKE;
import static com.balsamic.sejongmalsami.object.constants.LikeType.LIKE;
import static com.balsamic.sejongmalsami.object.constants.PostTier.CHEONMIN;
import static com.balsamic.sejongmalsami.object.constants.PostTier.JUNGIN;
import static com.balsamic.sejongmalsami.object.constants.PostTier.KING;
import static com.balsamic.sejongmalsami.object.constants.PostTier.YANGBAN;
import static com.balsamic.sejongmalsami.object.constants.YeopjeonAction.RECEIVE_DISLIKE;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.LikeType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.CommentLike;
import com.balsamic.sejongmalsami.object.mongo.DocumentBoardLike;
import com.balsamic.sejongmalsami.object.mongo.ExpHistory;
import com.balsamic.sejongmalsami.object.mongo.QuestionBoardLike;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.CommentLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.DocumentBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.RedisLockManager;
import com.balsamic.sejongmalsami.util.config.PostTierConfig;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {

  private static final Integer DEMOTION_DISLIKE_LIMIT = 20;

  private final MemberRepository memberRepository;
  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final DocumentBoardLikeRepository documentBoardLikeRepository;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonConfig yeopjeonConfig;
  private final ExpService expService;
  private final PostTierConfig postTierConfig;
  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final RedisLockManager redisLockManager;


  /**
   * <h3>질문글 or 답변글 좋아요 로직</h3>
   * <ul>
   *   <li>본인이 작성한 글에 좋아요 불가</li>
   *   <li>이미 좋아요 누른 글에 중복 요청 불가</li>
   *   <li>좋아요 받은 사용자 엽전/경험치 증가</li>
   * </ul>
   *
   * @param command memberId, postId, contentType
   * @return 질문게시판 좋아요 내역
   */
  @Transactional
  public QuestionDto questionBoardLike(QuestionCommand command) {
    return processLikeRequest(
        command.getMemberId(),
        command.getPostId(),
        command.getContentType(),
        LIKE
    );
  }

  /**
   * <h3>자료 글 or 자료 요청 글 좋아요/싫어요 로직</h3>
   * <p>자료 글 로직</p>
   * <ul>
   *   <li>자료 등급에 접근 불가능한 사용자 검증</li>
   *   <li>본인이 작성한 글에 좋아요/싫어요 불가</li>
   *   <li>이미 좋아요/싫어요 누른 글에 중복 요청 불가</li>
   *   <li>좋아요 받은 사용자 엽전/경험치 증가</li>
   *   <li>싫어요 받은 사용자 엽전 감소</li>
   *   <li>(좋아요-싫어요) 개수 특정 기준 이상 도달 시 자료 등급 업데이트</li>
   *   <li>자료 등급 강등은 싫어요 개수가 20개 이상인 자료 대상으로 진행. 싫어요 20개 미만인 글들은 강등 방어</li>
   * </ul>
   * <p>자료 요청 글 로직</p>
   * <ul>
   *   <li>본인이 작성한 글에 좋아요 불가능</li>
   *   <li>이미 좋아요 누른 글에 중복 요청 불가</li>
   *   <li>좋아요 받은 사용자 엽전/경험치 증가</li>
   * </ul>
   *
   * @param command memberId, documentPostId, contentType, reactionType
   * @return 자료 or 자료 요청 글 좋아요/싫어요 히스토리
   */
  @Transactional
  public DocumentDto documentBoardLike(DocumentCommand command) {
    return processLikeRequest(
        command.getMemberId(),
        command.getDocumentPostId(),
        command.getContentType(),
        command.getLikeType()
    );
  }

  /**
   * <h3>댓글 좋아요 로직</h3>
   * <p>해당 댓글 좋아요 개수 증가, 엽전 및 경험치 변동</p>
   * <p>본인이 작성한 댓글에 좋아요 불가</p>
   *
   * @param command memberId, postId
   * @return 댓글 좋아요 내역
   */
  @Transactional
  public CommentDto commentLike(CommentCommand command) {
    return processLikeRequest(
        command.getMemberId(),
        command.getPostId(),
        command.getContentType(),
        LIKE
    );
  }


  /**
   * <h3>공통 좋아요/싫어요 처리 로직</h3>
   *
   * @param memberId    로그인 회원
   * @param postId      게시글
   * @param contentType 유형
   * @param likeType    좋아요/싫어요
   * @return
   */
  private <T> T processLikeRequest(UUID memberId, UUID postId, ContentType contentType, LikeType likeType) {
    // 락 획득 시도 (락 키는 게시글 PK)
    String lockKey = "lock:like" + postId;

    return redisLockManager.executeLock(lockKey, () -> {

      // 회원 조회
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      // 글 작성자
      Member writer;

      QuestionPost questionPost;
      AnswerPost answerPost;
      DocumentPost documentPost;
      DocumentRequestPost documentRequestPost;
      Comment comment;
      PostTier preTier = null;

      // ContentType 에 따른 작성글 조회
      if (contentType.equals(QUESTION)) { // 질문글
        questionPost = questionPostRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
        writer = questionPost.getMember();
      } else if (contentType.equals(ANSWER)) { // 답변글
        answerPost = answerPostRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
        writer = answerPost.getMember();
      } else if (contentType.equals(DOCUMENT)) { // 자료글
        documentPost = documentPostRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
        writer = documentPost.getMember();
        preTier = documentPost.getPostTier(); // 자료 글 기존 등급
        canAccessDocumentBoard(curMember, documentPost.getPostTier()); // 해당 자료 글 등급 접근 여부 검증
      } else if (contentType.equals(DOCUMENT_REQUEST)) { // 자료요청글
        documentRequestPost = documentRequestPostRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));
        writer = documentRequestPost.getMember();
      } else if (contentType.equals(COMMENT)) { // 댓글
        comment = commentRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        writer = comment.getMember();
      } else { // 잘못된 contentType
        log.error("요청된 ContentType: {}", contentType);
        throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
      }

      // 자기 자신에게 요청여부 검증
      validateSelfAction(curMember, writer);

      // 이미 좋아요/싫어요를 누른 글인지 검증
      checkAlreadyAction(memberId, postId, contentType);

      // 엽전 및 경험치 증감
      YeopjeonHistory yeopjeonHistory = null;
      ExpHistory expHistory = null;

      try {
        if (likeType.equals(LIKE)) { // 좋아요를 눌렀을 경우
          yeopjeonHistory = yeopjeonService.processYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);
          expHistory = expService.processExp(writer, ExpAction.RECEIVE_LIKE);
        } else if (likeType.equals(DISLIKE) && contentType.equals(DOCUMENT)) { // 싫어요를 눌렀을 경우 (자료글만 가능)
          yeopjeonHistory = yeopjeonService.processYeopjeon(writer, RECEIVE_DISLIKE);
        } else {
          log.error("ContentType, ReactionType 요청이 잘못되었습니다. ContentType: {}, ReactionType: {}",
              contentType, likeType);
          throw new CustomException(ErrorCode.INVALID_REACTION_TYPE);
        }
      } catch (Exception e) { // 엽전 or 경험치 처리 중 오류 발생
        log.error("엽전/경험치 처리 중 오류 발생", e);
        throw new CustomException(ErrorCode.YEOPJEON_SAVE_ERROR);
      }

      // 좋아요/싫어요 증가
      try {
        applyAction(memberId, postId, contentType, likeType);
        if (contentType.equals(QUESTION)) {
          QuestionBoardLike questionBoardLike = QuestionBoardLike.builder()
              .memberId(memberId)
              .questionBoardId(postId)
              .contentType(contentType)
              .build();
          questionBoardLikeRepository.save(questionBoardLike);
          return (T) QuestionDto.builder()
              .questionBoardLike(questionBoardLike)
              .build();
        } else if (contentType.equals(DOCUMENT)) { // 자료글인 경우 등급 변동 계산
          calculateNewTier(postId, likeType);
          DocumentBoardLike documentBoardLike = DocumentBoardLike.builder()
              .memberId(memberId)
              .documentBoardId(postId)
              .contentType(contentType)
              .likeType(likeType)
              .build();
          documentBoardLikeRepository.save(documentBoardLike);
          return (T) DocumentDto.builder()
              .documentBoardLike(documentBoardLike)
              .build();
        } else if (contentType.equals(DOCUMENT_REQUEST)) {
          DocumentBoardLike documentBoardLike = DocumentBoardLike.builder()
              .memberId(memberId)
              .documentBoardId(postId)
              .contentType(contentType)
              .likeType(likeType)
              .build();
          documentBoardLikeRepository.save(documentBoardLike);
          return (T) DocumentDto.builder()
              .documentBoardLike(documentBoardLike)
              .build();
        } else if (contentType.equals(COMMENT)) {
          CommentLike commentLike = CommentLike.builder()
              .memberId(memberId)
              .commentId(postId)
              .contentType(contentType)
              .build();
          commentLikeRepository.save(commentLike);
          return (T) CommentDto.builder()
              .commentLike(commentLike)
              .build();
        } else { // 잘못된 contentType
          log.error("요청된 ContentType: {}", contentType);
          throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
        }
      } catch (Exception e) { // 좋아요/싫어요 변동 중 오류 발생
        log.error("좋아요/싫어요 변동 중 오류가 발생했습니다.", e);
        if (contentType.equals(DOCUMENT)) {
          rollbackTier(postId, preTier); // 자료 글 등급 롤백
        }
        rollbackAction(postId, contentType, likeType); // 좋아요/싫어요 수 롤백
        if (likeType.equals(LIKE)) {
          yeopjeonService.rollbackYeopjeonTransaction(writer, YeopjeonAction.RECEIVE_LIKE, yeopjeonHistory);
          expService.rollbackExpTransaction(writer, ExpAction.RECEIVE_LIKE, expHistory);
        } else if (likeType.equals(DISLIKE) && contentType.equals(DOCUMENT)) {
          yeopjeonService.rollbackYeopjeonTransaction(writer, RECEIVE_DISLIKE, yeopjeonHistory);
        } else {
          log.error("ContentType, ReactionType 요청이 잘못되었습니다. ContentType: {}, ReactionType: {}",
              contentType, likeType);
          throw new CustomException(ErrorCode.INVALID_REACTION_TYPE);
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    });
  }

  // 자기 자신에게 요청했는지 검증
  private void validateSelfAction(Member curMember, Member writer) {
    if (curMember.equals(writer)) {
      log.error("본인 글에 좋아요/싫어요를 누를 수 없습니다. 로그인 사용자: {}, 글 작성자: {}",
          curMember.getStudentId(), writer.getStudentId());
      throw new CustomException(ErrorCode.SELF_ACTION_NOT_ALLOWED);
    }
  }

  /**
   * <h3>이미 좋아요/싫어요를 눌렀는지 검증</h3>
   *
   * @param memberId    로그인 회원
   * @param postId      게시물
   * @param contentType 게시물 유형
   */
  private void checkAlreadyAction(UUID memberId, UUID postId, ContentType contentType) {
    boolean exists = false;

    switch (contentType) {
      case QUESTION, ANSWER ->
          exists = questionBoardLikeRepository.existsByQuestionBoardIdAndMemberId(postId, memberId);
      case DOCUMENT, DOCUMENT_REQUEST ->
          exists = documentBoardLikeRepository.existsByDocumentBoardIdAndMemberId(postId, memberId);
      case COMMENT -> exists = commentLikeRepository.existsByCommentIdAndMemberId(postId, memberId);
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    if (exists) {
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      log.error("이미 좋아요/싫어요를 누른 글입니다. postId: {}, 로그인한 사용자: {}",
          postId, curMember.getStudentId());
      throw new CustomException(ErrorCode.ALREADY_ACTION);
    }
  }

  /**
   * <h3>좋아요/싫어요 개수 변동 메서드</h3>
   * <p>해당 글의 좋아요 or 싫어요 개수 증가</p>
   *
   * @param memberId    로그인 회원
   * @param postId      게시물
   * @param contentType 게시물 유형
   * @param likeType    좋아요/싫어요 유형 (자료글)
   */
  private void applyAction(UUID memberId, UUID postId, ContentType contentType, LikeType likeType) {
    if (contentType.equals(QUESTION)) { // 질문 글 좋아요 증가
      QuestionPost questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
      questionPost.increaseLikeCount();
      questionPostRepository.save(questionPost);
    } else if (contentType.equals(ANSWER)) { // 답변 좋아요 증가
      AnswerPost answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
      answerPost.increaseLikeCount();
      answerPostRepository.save(answerPost);
    } else if (contentType.equals(DOCUMENT)) { // 자료 글 좋아요 / 싫어요 증가
      DocumentPost documentPost = documentPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
      if (likeType.equals(LIKE)) { // 자료 글 좋아요 증가
        documentPost.increaseLikeCount();
        documentPostRepository.save(documentPost);
      } else if (likeType.equals(DISLIKE)) { // 자료 글 싫어요 증가
        documentPost.increaseDislikeCount();
        documentPostRepository.save(documentPost);
      } else {
        log.error("잘못된 ReactionType 입니다. 요청 ReactionType: {}", likeType);
        throw new CustomException(ErrorCode.INVALID_REACTION_TYPE);
      }
    } else if (contentType.equals(DOCUMENT_REQUEST)) { // 자료 요청 글 좋아요 증가
      DocumentRequestPost documentRequestPost = documentRequestPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));
      documentRequestPost.increaseLikeCount();
      documentRequestPostRepository.save(documentRequestPost);
    } else if (contentType.equals(COMMENT)) { // 댓글 좋아요 증가
      Comment comment = commentRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
      comment.increaseLikeCount();
      commentRepository.save(comment);
    } else {
      log.error("잘못된 ContentType입니다. 요청 ContentType: {}", contentType);
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }
  }

  /**
   * <h3>좋아요/싫어요 롤백</h3>
   * <p>해당 글의 좋아요 or 싫어요 개수 롤백</p>
   *
   * @param postId      게시물
   * @param contentType 게시물 유형
   * @param likeType    좋아요/싫어요 유형 (자료글)
   */
  private void rollbackAction(UUID postId, ContentType contentType, LikeType likeType) {
    if (contentType.equals(QUESTION)) { // 질문 글 좋아요 롤백
      QuestionPost questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
      questionPost.decreaseLikeCount();
      questionPostRepository.save(questionPost);
    } else if (contentType.equals(ANSWER)) { // 답변 좋아요 롤백
      AnswerPost answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
      answerPost.decreaseLikeCount();
      answerPostRepository.save(answerPost);
    } else if (contentType.equals(DOCUMENT)) { // 자료 글 좋아요 / 싫어요 롤백
      DocumentPost documentPost = documentPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
      if (likeType.equals(LIKE)) { // 자료 글 좋아요 롤백
        documentPost.decreaseLikeCount();
        documentPostRepository.save(documentPost);
      } else if (likeType.equals(DISLIKE)) { // 자료 글 싫어요 롤백
        documentPost.decreaseDislikeCount();
        documentPostRepository.save(documentPost);
      } else {
        log.error("잘못된 ReactionType 입니다. 요청 ReactionType: {}", likeType);
        throw new CustomException(ErrorCode.INVALID_REACTION_TYPE);
      }
    } else if (contentType.equals(DOCUMENT_REQUEST)) { // 자료 요청 글 좋아요 롤백
      DocumentRequestPost documentRequestPost = documentRequestPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));
      documentRequestPost.decreaseLikeCount();
      documentRequestPostRepository.save(documentRequestPost);
    }
  }

  /**
   * <h3>자료 게시판 접근 권한 검증 메서드</h3>
   *
   * @param curMember 로그인 회원
   * @param postTier  자료 등급
   */
  private void canAccessDocumentBoard(Member curMember, PostTier postTier) {
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(curMember);

    // 게시판 접근 가능 여부 확인
    if (postTier.equals(CHEONMIN)) { // 천민 게시판 접근 시
      log.info("천민 게시판 접근, 현재 사용자 {}의 엽전개수: {}", curMember.getStudentId(), yeopjeon.getYeopjeon());
    } else if (postTier.equals(JUNGIN)) { // 중인 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getJunginRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 중인게시판에 접근할 수 없습니다.", curMember.getStudentId());
        log.error("중인 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getJunginRequirement(),
            yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else if (postTier.equals(YANGBAN)) { // 양반 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getYangbanRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 양반게시판에 접근할 수 없습니다.", curMember.getStudentId());
        log.error("양반 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getYangbanRequirement(),
            yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else if (postTier.equals(KING)) { // 왕 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getKingRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 왕 게시판에 접근할 수 없습니다.", curMember.getStudentId());
        log.error("왕 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getKingRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else {
      throw new CustomException(ErrorCode.INVALID_POST_TIER);
    }
  }

  /**
   * <h3>자료 글 등급 승급/강등 로직</h3>
   *
   * @param postId   자료 글 PK
   * @param likeType 좋아요/싫어요 유형
   */
  private void calculateNewTier(UUID postId, LikeType likeType) {
    DocumentPost post = documentPostRepository.findById(postId)
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    // 현재 자료 등급
    PostTier curTier = post.getPostTier();

    // 자료 점수 계산
    int score = post.getLikeCount() - post.getDislikeCount();

    // 좋아요가 요청된 경우 (승급여부만 확인)
    if (likeType.equals(LikeType.LIKE)) {
      if (score >= postTierConfig.getLikeRequirementKing() && curTier.equals(YANGBAN)) {
        log.info("해당 글의 점수가 {} 에 도달하여 왕 등급으로 승급합니다. 현재 점수: {}", postTierConfig.getLikeRequirementKing(), score);
        post.setPostTier(KING);
      } else if (score >= postTierConfig.getLikeRequirementYangban() && curTier.equals(JUNGIN)) {
        log.info("해당 글의 점수가 {} 에 도달하여 양반 등급으로 승급합니다. 현재 점수: {}", postTierConfig.getLikeRequirementYangban(), score);
        post.setPostTier(YANGBAN);
      } else if (score >= postTierConfig.getLikeRequirementJungin() && curTier.equals(CHEONMIN)) {
        log.info("해당 글의 점수가 {} 에 도달하여 중인 등급으로 승급합니다. 현재 점수: {}", postTierConfig.getLikeRequirementJungin(), score);
        post.setPostTier(JUNGIN);
      }
    } else if (likeType.equals(LikeType.DISLIKE)) { // 싫어요가 요청된 경우 (강등여부만 확인)
      if (post.getDislikeCount() < DEMOTION_DISLIKE_LIMIT) { // 싫어요 개수가 20개 미만인 경우 강등여부 확인 X
        return;
      }
      if (score < postTierConfig.getLikeRequirementJungin() && curTier.equals(JUNGIN)) {
        log.info("해당 글의 점수가 {} 보다 낮아 천민 등급으로 강등됩니다. 현재 점수: {}", postTierConfig.getLikeRequirementJungin(), score);
        post.setPostTier(CHEONMIN);
      } else if (score < postTierConfig.getLikeRequirementYangban() && curTier.equals(YANGBAN)) {
        log.info("해당 글의 점수가 {} 보다 낮아 중인 등급으로 강등됩니다. 현재 점수: {}", postTierConfig.getLikeRequirementYangban(), score);
        post.setPostTier(JUNGIN);
      } else if (score < postTierConfig.getLikeRequirementKing() && curTier.equals(KING)) {
        log.info("해당 글의 점수가 {} 보다 낮아 양반 등급으로 강등됩니다. 현재 점수: {}", postTierConfig.getLikeRequirementKing(), score);
        post.setPostTier(YANGBAN);
      }
    }

    // 등급 변동이 일어난 경우
    if (!curTier.equals(post.getPostTier())) {
      log.info("{} 글의 자료 등급이 {} 에서 {} 으로 변동되었습니다.",
          post.getDocumentPostId(),
          curTier.getDescription(),
          post.getPostTier().getDescription());
      documentPostRepository.save(post);
    }
  }

  /**
   * <h3>자료 등급 롤백 로직</h3>
   *
   * @param postId  자료 글 PK
   * @param preTier 변동 전 자료 등급
   */
  private void rollbackTier(UUID postId, PostTier preTier) {
    DocumentPost post = documentPostRepository.findById(postId)
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    post.setPostTier(preTier);
    documentPostRepository.save(post);
    log.info("{} 글의 등급이 {} 로 롤백되었습니다.", post.getDocumentPostId(), preTier);
  }
}
