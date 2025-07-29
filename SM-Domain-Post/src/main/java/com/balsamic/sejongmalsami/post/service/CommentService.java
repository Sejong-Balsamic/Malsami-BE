package com.balsamic.sejongmalsami.post.service;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.mongo.CommentLike;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.postgres.AnswerPost;
import com.balsamic.sejongmalsami.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.mongo.CommentLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
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
@Slf4j
@RequiredArgsConstructor
public class CommentService {

  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final CommentRepository commentRepository;
  private final CommentLikeRepository commentLikeRepository;
  private final MemberRepository memberRepository;
  private final com.balsamic.sejongmalsami.service.ExpService expService;

  /**
   * <h3>댓글 작성 로직
   * <p>ContentType 에 따른 글 or 답변에 댓글 작성
   * <p>작성자 경험치 증가 및 경험치 내역 저장
   * <p>댓글 작성 시 해당 글 or 답변 댓글 수 증가
   *
   * @param command memberId, content, postId, contentType, isPrivate
   * @return
   */
  @Transactional
  public CommentDto addComment(CommentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    UUID postId = command.getPostId();
    ContentType contentType = command.getContentType();

    QuestionPost questionPost;
    AnswerPost answerPost;
    DocumentRequestPost documentRequestPost;

    // ContentType 에 따른 댓글 처리
    // FIXME: 자료, 자료요청, 공지사항, 댓글 ContentType 처리
    if (contentType.equals(ContentType.QUESTION)) { // 질문글
      questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

      // 댓글 수 증가
      log.info("댓글 작성 전 댓글 수: {}", questionPost.getCommentCount());
      questionPost.increaseCommentCount();
      questionPostRepository.save(questionPost);
      log.info("댓글 작성 후 댓글 수: {}", questionPost.getCommentCount());
    } else if (contentType.equals(ContentType.ANSWER)) { // 답변
      answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));

      // 댓글 수 증가
      log.info("댓글 작성 전 댓글 수: {}", answerPost.getCommentCount());
      answerPost.increaseCommentCount();
      answerPostRepository.save(answerPost);
      log.info("댓글 작성 후 댓글 수: {}", answerPost.getCommentCount());
    } else if (contentType.equals(ContentType.DOCUMENT)) { // 자료글
      DocumentPost documentPost = documentPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

      // 댓글 수 증가
      log.info("댓글 작성 전 댓글 수: {}", documentPost.getCommentCount());
      documentPost.increaseCommentCount();
      log.info("댓글 작성 후 댓글 수: {}", documentPost.getCommentCount());
    } else if (contentType.equals(ContentType.DOCUMENT_REQUEST)) { // 자료 요청 글
      documentRequestPost = documentRequestPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));

      // 댓글 수 증가
      log.info("댓글 작성 전 댓글 수: {}", documentRequestPost.getCommentCount());
      documentRequestPost.increaseCommentCount();
      documentRequestPostRepository.save(documentRequestPost);
      log.info("댓글 작성 후 댓글 수: {}", documentRequestPost.getCommentCount());
    } else if (contentType.equals(ContentType.NOTICE)) {

    } else {
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 댓글 작성자 경험치 증가 및 경험치 히스토리 저장
    expService.processExp(member, ExpAction.CREATE_COMMENT);

    // 댓글 엔티티 저장
    Comment comment = commentRepository.save(Comment.builder()
        .member(member)
        .content(command.getContent())
        .postId(postId)
        .contentType(contentType)
        .likeCount(0)
        .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
        .build());

    return CommentDto.builder()
        .comment(comment)
        .build();
  }

  /**
   * <h3>특정 글에 작성된 모든 댓글 조회 로직
   *
   * @param command <p>memberId: 로그인 사용자 PK</p>
   *                <p>postId: 특정 글 PK</p>
   *                <p>contentType: 글 Type</p>
   *                <p>pageNumber: n번째 페이지</p>
   *                <p>pageSize: n개의 데이터</p>
   * @return
   */
  @Transactional(readOnly = true)
  public CommentDto getAllCommentsByPostId(CommentCommand command) {
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("createdDate").descending()
    );

    Page<Comment> commentPage = commentRepository.findByPostIdAndContentType(
        command.getPostId(),
        command.getContentType(),
        pageable
    );

    // 댓글 PK 목록 가져오기
    List<UUID> commentIds = commentPage.getContent().stream()
        .map(Comment::getCommentId)
        .collect(Collectors.toList());

    // 현재 사용자가 좋아요를 누른 댓글 PK 조회
    Set<UUID> likedCommentIds = commentLikeRepository
        .findAllByCommentIdInAndMemberId(commentIds, command.getMemberId())
        .stream().map(CommentLike::getCommentId)
        .collect(Collectors.toSet());

    // 각 댓글에 좋아요 여부 설정
    commentPage.forEach(comment -> comment
        .setIsLiked(likedCommentIds.contains(comment.getCommentId())));

    return CommentDto.builder()
        .commentsPage(commentPage)
        .build();
  }
}
