package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.LogUtils.superLog;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
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
  private final CommentRepository commentRepository;
  private final MemberRepository memberRepository;
  private final ExpService expService;

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

    // ContentType 에 따른 댓글 처리
    // FIXME: 자료, 자료요청, 공지사항, 댓글 ContentType 처리
    if (contentType.equals(ContentType.QUESTION)) {
      questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

      // 댓글 수 증가
      log.info("댓글 작성 전 댓글 수: {}", questionPost.getCommentCount());
      questionPost.increaseCommentCount();
      questionPostRepository.save(questionPost);
      log.info("댓글 작성 후 댓글 수: {}", questionPost.getCommentCount());
      superLog(questionPost);
    } else if (contentType.equals(ContentType.ANSWER)) {
      answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));

      // 댓글 수 증가
      log.info("댓글 작성 전 댓글 수: {}", answerPost.getCommentCount());
      answerPost.increaseCommentCount();
      answerPostRepository.save(answerPost);
      log.info("댓글 작성 후 댓글 수: {}", answerPost.getCommentCount());
      superLog(answerPost);
    } else if (contentType.equals(ContentType.DOCUMENT)) {

    } else if (contentType.equals(ContentType.DOCUMENT_REQUEST)) {

    } else if (contentType.equals(ContentType.NOTICE)) {

    } else if (contentType.equals(ContentType.COMMENT)) {

    } else {
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 댓글 작성자 경험치 증가 및 경험치 히스토리 저장
    expService.updateExpAndSaveExpHistory(member, ExpAction.CREATE_COMMENT);

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
   * @param command <p>postId: 특정 글 PK</p>
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

    return CommentDto.builder()
        .commentsPage(commentPage)
        .build();
  }
}
