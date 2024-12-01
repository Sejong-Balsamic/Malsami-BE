package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.PostTier.*;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.constants.ReactionType;
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

    // 로그인 사용자
    Member curMember = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    UUID postId = command.getPostId();

    Member writer;

    // 질문 글 or 답변 글
    QuestionPost questionPost = null;
    AnswerPost answerPost = null;

    // 해당 글 좋아요 증가 가능 여부 확인
    if (command.getContentType().equals(ContentType.QUESTION)) {
      questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
      writer = questionPost.getMember();
      validateSelfLike(curMember, writer); // 본인이 작성한 글에 좋아요 불가
      isMemberAlreadyAction(postId, curMember.getMemberId()); // 이미 좋아요 누른 글에 중복 요청 불가
    } else if (command.getContentType().equals(ContentType.ANSWER)) {
      answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
      writer = answerPost.getMember();
      validateSelfLike(curMember, writer); // 본인이 작성한 글에 좋아요 불가
      isMemberAlreadyAction(postId, curMember.getMemberId()); // 이미 좋아요 누른 글에 중복 요청 불가
    } else {
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 좋아요 받은 사용자 엽전 개수 증가 및 엽전 히스토리 저장 - A
    YeopjeonHistory writerYeopjeonHistory = yeopjeonService
        .processYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);

    // 좋아요 받은 사용자 경험치 증가 및 경험치 히스토리 저장 - B
    ExpHistory writerExpHistory = null;
    try {
      writerExpHistory = expService.updateExpAndSaveExpHistory(writer, ExpAction.RECEIVE_LIKE);
    } catch (Exception e) { // B 실패시 A 롤백
      yeopjeonService.rollbackYeopjeonTransaction(
          writer,
          YeopjeonAction.RECEIVE_LIKE,
          writerYeopjeonHistory
      );
      throw new CustomException(ErrorCode.EXP_SAVE_ERROR); // 트랜잭션 롤백을 위해 예외 던지기
    }

    // 좋아요 증가 및 MongoDB에 좋아요 내역 저장 - C
    try {
      // 좋아요 증가
      increaseLikeCount(questionPost, answerPost, null, null);
      return QuestionDto.builder()
          .questionBoardLike(questionBoardLikeRepository.save(QuestionBoardLike.builder()
              .memberId(curMember.getMemberId())
              .questionBoardId(postId)
              .contentType(command.getContentType())
              .build()))
          .build();
    } catch (Exception e) {
      log.error("좋아요 내역 저장 실패 및 롤백: {}", e.getMessage());

      // 좋아요 수 롤백
      rollbackLikeCount(questionPost, answerPost, null, null);

      // 엽전, 경험치 및 히스토리 롤백 - C 실패시 A, B 롤백
      expService.rollbackExpAndDeleteExpHistory(
          writer, ExpAction.RECEIVE_LIKE, writerExpHistory);
      yeopjeonService.rollbackYeopjeonTransaction(
          writer, YeopjeonAction.RECEIVE_LIKE, writerYeopjeonHistory);
      throw new CustomException(ErrorCode.QUESTION_BOARD_LIKE_SAVE_ERROR);
    }
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
   *   <li>중인 이상 접근 가능</li>
   *   <li>본인이 작성한 글에 좋아요 불가능</li>
   *   <li>이미 좋아요 누른 글에 중복 요청 불가</li>
   *   <li>좋아요 받은 사용자 엽전/경험치 증가</li>
   * </ul>
   *
   * @param command memberId, documentPostId, contentType, reactionType
   * @return
   */
  @Transactional
  public DocumentDto documentBoardLike(DocumentCommand command) {

    // 로그인 사용자
    Member curMember = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    UUID postId = command.getDocumentPostId();

    Member writer;

    // 자료 글 or 자료 요청 글
    DocumentPost documentPost = null;
    DocumentRequestPost documentRequestPost = null;

    // 1. 해당 글 좋아요/싫어요 가능 여부 확인
    if (command.getContentType().equals(ContentType.DOCUMENT)) {
      documentPost = documentPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
      writer = documentPost.getMember();
      validateSelfLike(curMember, writer); // 본인이 작성한 글에 좋아요/싫어요 불가
      isMemberAlreadyAction(postId, curMember.getMemberId()); // 이미 좋아요/싫어요 누른 글에 중복 요청 불가
      canAccessDocumentBoard(curMember, documentPost.getPostTier()); // 해당 자료 등급에 접근 가능 여부 확인
    } else if (command.getContentType().equals(ContentType.DOCUMENT_REQUEST)) {
      documentRequestPost = documentRequestPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));
      writer = documentRequestPost.getMember();
      validateSelfLike(curMember, writer); // 본인이 작성한 글에 좋아요 불가
      isMemberAlreadyAction(postId, curMember.getMemberId()); // 이미 좋아요/싫어요 누른 글에 중복 요청 불가
      canAccessDocumentBoard(curMember, JUNGIN); // '중인' 이상 유저 접근 가능
    } else {
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 2. 좋아요/싫어요에 따른 엽전 및 경험치 증감 로직
    if (command.getReactionType().equals(ReactionType.LIKE)) { // 좋아요를 누른 경우
      // 좋아요 받은 사용자 (writer) 엽전 증가 및 엽전 히스토리 저장 - A
      YeopjeonHistory writerYeopjeonHistory = yeopjeonService
          .processYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);
      // 좋아요 받은 사용자 (writer) 경험치 증가 및 경험치 히스토리 저장 - B
      ExpHistory writerExpHistory = null;
      try {
        writerExpHistory = expService.updateExpAndSaveExpHistory(writer, ExpAction.RECEIVE_LIKE);
      } catch (Exception e) { // B 실패시 A 롤백
        yeopjeonService.rollbackYeopjeonTransaction(
            writer,
            YeopjeonAction.RECEIVE_LIKE,
            writerYeopjeonHistory
        );
        throw new CustomException(ErrorCode.EXP_SAVE_ERROR); // 트랜잭션 롤백을 위해 예외 던지기
      }
      // 좋아요 증가 및 MongoDB에 좋아요 내역 저장 - C
      PostTier previousTier = null;
      try {
        // 좋아요 증가
        increaseLikeCount(null, null, documentPost, documentRequestPost);
        if (documentPost != null) { // 자료글에 좋아요를 누른 경우
          previousTier = documentPost.getPostTier(); // 등급 변동 전 등급 저장
          updatePostTier(documentPost, command.getReactionType()); // 등급 변동 로직 호출
        }
        return DocumentDto.builder()
            .documentBoardLike(documentBoardLikeRepository.save(DocumentBoardLike.builder()
                .memberId(curMember.getMemberId())
                .documentBoardId(postId)
                .contentType(command.getContentType())
                .reactionType(command.getReactionType())
                .build()))
            .build();
      } catch (Exception e) { // C 실패시 A, B 롤백
        log.error("좋아요 내역 저장 실패 및 롤백: {}", e.getMessage());

        // 등급 변동 롤백
        if (documentPost != null) {
          rollbackPostTier(documentPost, previousTier); // 등급 롤백
        }

        // 좋아요 수 롤백
        rollbackLikeCount(null, null, documentPost, documentRequestPost);

        // 엽전, 경험치 및 히스토리 롤백
        expService.rollbackExpAndDeleteExpHistory(
            writer, ExpAction.RECEIVE_LIKE, writerExpHistory);
        yeopjeonService.rollbackYeopjeonTransaction(
            writer, YeopjeonAction.RECEIVE_LIKE, writerYeopjeonHistory);
        throw new CustomException(ErrorCode.DOCUMENT_BOARD_LIKE_SAVE_ERROR);
      }
    } else if (command.getReactionType().equals(ReactionType.DISLIKE)) { // 싫어요를 누른 경우
      if (documentPost == null) {
        log.error("요청한 PK값에 해당하는 자료 글이 존재하지 않습니다. 싫어요는 자료글에만 요청할 수 있습니다. 요청 postId: {}", postId);
        throw new CustomException(ErrorCode.INVALID_REQUEST);
      }
      // 싫어요 받은 사용자 (writer) 엽전 감소 및 엽전 히스토리 저장 - A
      YeopjeonHistory writerYeopjeonHistory = yeopjeonService
          .processYeopjeon(writer, YeopjeonAction.RECEIVE_DISLIKE);
      // 싫어요 증가 및 MongoDB에 싫어요 내역 저장 - B
      PostTier previousTier = null;
      try {
        // 싫어요 증가
        documentPost.increaseDislikeCount();
        previousTier = documentPost.getPostTier(); // 등급 변동 전 등급 저장
        updatePostTier(documentPost, command.getReactionType()); // 등급 변동 로직 호출
        return DocumentDto.builder()
            .documentBoardLike(documentBoardLikeRepository.save(DocumentBoardLike.builder()
                .memberId(curMember.getMemberId())
                .documentBoardId(postId)
                .contentType(command.getContentType())
                .reactionType(command.getReactionType())
                .build()))
            .build();
      } catch (Exception e) { // B 실패시 A 롤백
        log.error("싫어요 내역 저장 실패 및 롤백: {}", e.getMessage());

        rollbackPostTier(documentPost, previousTier); // 등급 롤백

        // 싫어요 수 롤백
        documentPost.decreaseDislikeCount();
        documentPostRepository.save(documentPost);

        // 엽전 및 히스토리 롤백
        yeopjeonService.rollbackYeopjeonTransaction(
            writer, YeopjeonAction.RECEIVE_DISLIKE, writerYeopjeonHistory);
        throw new CustomException(ErrorCode.DOCUMENT_BOARD_LIKE_SAVE_ERROR);
      }
    } else { // 요청이 잘못된 경우
      log.error("잘못된 ReactionType 입니다. 요청한 type: {}", command.getReactionType());
      throw new CustomException(ErrorCode.INVALID_REACTION_TYPE);
    }
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

    // 로그인 사용자
    Member curMember = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    UUID postId = command.getPostId();

    Comment comment = commentRepository.findById(postId)
        .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

    // 댓글 작성자
    Member commentWriter = comment.getMember();

    // 본인이 작성한 댓글에 좋아요 불가
    validateSelfLike(curMember, commentWriter);

    // 이미 좋아요를 누른 경우 검증
    if (commentLikeRepository.existsByCommentIdAndMemberId(postId, curMember.getMemberId())) {
      log.error("이미 좋아요를 누른 댓글입니다. CommentId: {}, 로그인한 사용자: {}",
          postId, curMember.getStudentId());
      throw new CustomException(ErrorCode.ALREADY_ACTION);
    }

    // 좋아요 받은 사용자 엽전 개수 증가 및 엽전 히스토리 저장 - A
    YeopjeonHistory writerYeopjeonHistory = yeopjeonService
        .processYeopjeon(commentWriter, YeopjeonAction.RECEIVE_LIKE);

    // 좋아요 받은 사용자 경험치 증가 및 경험치 히스토리 저장 - B
    ExpHistory writerExpHistory = null;
    try {
      writerExpHistory = expService.updateExpAndSaveExpHistory(commentWriter, ExpAction.RECEIVE_LIKE);
    } catch (Exception e) { // B 실패시 A 롤백
      yeopjeonService.rollbackYeopjeonTransaction(
          commentWriter,
          YeopjeonAction.RECEIVE_LIKE,
          writerYeopjeonHistory
      );
      throw new CustomException(ErrorCode.EXP_SAVE_ERROR); // 트랜잭션 롤백을 위해 예외 던지기
    }

    // 좋아요 증가 및 MongoDB에 좋아요 내역 저장 - C
    try {
      // 좋아요 증가, contentType 댓글 고정
      comment.increaseLikeCount();
      commentRepository.save(comment); // 변경 사항 저장
      return CommentDto.builder()
          .commentLike(commentLikeRepository.save(CommentLike.builder()
              .memberId(command.getMemberId())
              .commentId(postId)
              .contentType(ContentType.COMMENT)
              .build()))
          .build();
    } catch (Exception e) {
      log.error("좋아요 내역 저장 실패 및 롤백: {}", e.getMessage());

      // 좋아요 수 롤백
      comment.rollbackLikeCount();
      commentRepository.save(comment); // 변경 사항 저장

      // 엽전, 경험치 및 히스토리 롤백 - C 실패시 A, B 롤백
      expService.rollbackExpAndDeleteExpHistory(
          commentWriter, ExpAction.RECEIVE_LIKE, writerExpHistory);
      yeopjeonService.rollbackYeopjeonTransaction(
          commentWriter, YeopjeonAction.RECEIVE_LIKE, writerYeopjeonHistory);
      throw new CustomException(ErrorCode.COMMENT_LIKE_HISTORY_SAVE_ERROR);
    }
  }

  // 로그인 된 사용자와 작성자가 같은 경우 검증 메서드
  private static void validateSelfLike(Member member, Member writer) {
    if (member.getMemberId().equals(writer.getMemberId())) {
      log.error("본인 글에 좋아요를 누를 수 없습니다. 로그인된 사용자: {}, 글 작성자: {}",
          member.getStudentId(), writer.getStudentId());
      throw new CustomException(ErrorCode.SELF_ACTION_NOT_ALLOWED);
    }
  }

  // 이미 좋아요/싫어요를 누른 경우 검증 메서드
  private void isMemberAlreadyAction(UUID postId, UUID memberId) {
    if (questionBoardLikeRepository.existsByQuestionBoardIdAndMemberId(postId, memberId) ||
        documentBoardLikeRepository.existsByDocumentBoardIdAndMemberId(postId, memberId)) {
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      log.error("이미 좋아요를 누른 글입니다. PostId: {}, 로그인한 사용자: {}",
          postId, curMember.getStudentId());
      throw new CustomException(ErrorCode.ALREADY_ACTION);
    }
  }

  // 좋아요 증가 메서드
  private void increaseLikeCount(
      QuestionPost questionPost,
      AnswerPost answerPost,
      DocumentPost documentPost,
      DocumentRequestPost documentRequestPost
  ) {
    if (questionPost != null) {
      questionPost.increaseLikeCount();
      questionPostRepository.save(questionPost);
    } else if (answerPost != null) {
      answerPost.increaseLikeCount();
      answerPostRepository.save(answerPost);
    } else if (documentPost != null) {
      documentPost.increaseLikeCount();
      documentPostRepository.save(documentPost);
    } else if (documentRequestPost != null) {
      documentRequestPost.increaseLikeCount();
      documentRequestPostRepository.save(documentRequestPost);
    } else {
      log.error("해당글에 좋아요를 증가시킬 수 없습니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
  }

  // 좋아요 롤백 메서드
  private void rollbackLikeCount(
      QuestionPost questionPost,
      AnswerPost answerPost,
      DocumentPost documentPost,
      DocumentRequestPost documentRequestPost) {
    if (questionPost != null) {
      questionPost.decreaseLikeCount();
      questionPostRepository.save(questionPost);
    } else if (answerPost != null) {
      answerPost.decreaseLikeCount();
      answerPostRepository.save(answerPost);
    } else if (documentPost != null) {
      documentPost.decreaseLikeCount();
      documentPostRepository.save(documentPost);
    } else if (documentRequestPost != null) {
      documentRequestPost.decreaseLikeCount();
      documentRequestPostRepository.save(documentRequestPost);
    } else {
      log.error("해당글에 좋아요를 롤백할 수 없습니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }
  }

  // 해당 자료 게시판 접근 가능 여부 판단 메소드
  private void canAccessDocumentBoard(Member member, PostTier postTier) {
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);

    // 게시판 접근 가능 여부 확인
    if (postTier.equals(CHEONMIN)) { // 천민 게시판 접근 시
      log.info("천민 게시판 접근, 현재 사용자 {}의 엽전개수: {}", member.getStudentId(), yeopjeon.getYeopjeon());
    } else if (postTier.equals(JUNGIN)) { // 중인 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getJunginRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 중인게시판에 접근할 수 없습니다.", member.getStudentId());
        log.error("중인 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getJunginRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else if (postTier.equals(YANGBAN)) { // 양반 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getYangbanRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 양반게시판에 접근할 수 없습니다.", member.getStudentId());
        log.error("양반 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getYangbanRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else if (postTier.equals(KING)) { // 왕 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getKingRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 왕 게시판에 접근할 수 없습니다.", member.getStudentId());
        log.error("왕 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getKingRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else {
      throw new CustomException(ErrorCode.INVALID_POST_TIER);
    }
  }

  // 자료 글 등급 승급/강등 로직
  private void updatePostTier(DocumentPost post, ReactionType reactionType) {

    if (post == null) {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    // 현재 자료 등급
    PostTier postTier = post.getPostTier();

    // 자료 점수 계산 (좋아요 수 - 싫어요 수)
    Integer score = post.getLikeCount() - post.getDislikeCount();

    // 좋아요가 요청된 경우 (승급여부만 확인)
    if (reactionType.equals(ReactionType.LIKE)) {
      if (score >= postTierConfig.getLikeRequirementKing() && postTier.equals(YANGBAN)) {
        log.info("해당 글의 점수가 {} 에 도달하여 왕 등급으로 승급합니다. 현재 점수: {}", postTierConfig.getLikeRequirementKing(), score);
        post.updatePostTier(KING);
      } else if (score >= postTierConfig.getLikeRequirementYangban() && postTier.equals(JUNGIN)) {
        log.info("해당 글의 점수가 {} 에 도달하여 양반 등급으로 승급합니다. 현재 점수: {}", postTierConfig.getLikeRequirementYangban(), score);
        post.updatePostTier(YANGBAN);
      } else if (score >= postTierConfig.getLikeRequirementJungin() && postTier.equals(CHEONMIN)) {
        log.info("해당 글의 점수가 {} 에 도달하여 중인 등급으로 승급합니다. 현재 점수: {}", postTierConfig.getLikeRequirementJungin(), score);
        post.updatePostTier(JUNGIN);
      }
    } else if (reactionType.equals(ReactionType.DISLIKE)) { // 싫어요가 요청된 경우 (강등여부만 확인)
      if (post.getDislikeCount() < DEMOTION_DISLIKE_LIMIT) { // 싫어요 개수가 20개 미만인 경우 강등여부 확인 X
        return;
      }
      if (score < postTierConfig.getLikeRequirementJungin() && postTier.equals(JUNGIN)) {
        log.info("해당 글의 점수가 {} 보다 낮아 천민 등급으로 강등됩니다. 현재 점수: {}", postTierConfig.getLikeRequirementJungin(), score);
        post.updatePostTier(CHEONMIN);
      } else if (score < postTierConfig.getLikeRequirementYangban() && postTier.equals(YANGBAN)) {
        log.info("해당 글의 점수가 {} 보다 낮아 중인 등급으로 강등됩니다. 현재 점수: {}", postTierConfig.getLikeRequirementYangban(), score);
        post.updatePostTier(JUNGIN);
      } else if (score < postTierConfig.getLikeRequirementKing() && postTier.equals(KING)) {
        log.info("해당 글의 점수가 {} 보다 낮아 양반 등급으로 강등됩니다. 현재 점수: {}", postTierConfig.getLikeRequirementKing(), score);
        post.updatePostTier(YANGBAN);
      }
    }

    // 등급 변동이 일어난 경우
    if (!postTier.equals(post.getPostTier())) {
      log.info("{} 글의 자료 등급이 {} 에서 {} 으로 변동되었습니다.",
          post.getDocumentPostId(),
          postTier.getDescription(),
          post.getPostTier().getDescription());
    }
  }

  // 등급 변동 롤백 메소드
  private void rollbackPostTier(DocumentPost post, PostTier previousTier) {
    post.updatePostTier(previousTier);
    documentPostRepository.save(post);
    log.info("{} 글의 등급이 {} 로 롤백되었습니다.", post.getDocumentTypes(), previousTier);
  }
}
