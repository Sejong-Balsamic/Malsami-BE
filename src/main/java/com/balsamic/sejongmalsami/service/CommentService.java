package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.LogUtils.lineLog;
import static com.balsamic.sejongmalsami.util.LogUtils.superLog;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
  private final CommentRepository commentRepository;
  private final MemberRepository memberRepository;
  private final ExpService expService;

  /**
   * <h3>댓글 작성 로직
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


    //TODO : CONTENT TYPE 여부에 대한 comment 처리 로직 필요
    QuestionPost questionPost = questionPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    //FIXME: 임시 로깅
    lineLog(null);
    log.info(String.valueOf(questionPost.getCommentCount()));
    lineLog(null);

    questionPost.increaseCommentCount();
    QuestionPost updatedQuestionPost = questionPostRepository.save(questionPost);

    //FIXME: 임시 로깅
    lineLog(null);
    log.info(String.valueOf(updatedQuestionPost.getCommentCount()));
    lineLog(null);

    // 댓글 작성자 경험치 증가 및 경험치 히스토리 저장
    expService.updateExpAndSaveExpHistory(member, ExpAction.CREATE_COMMENT);

    // 해당 글 or 답변 댓글 수 증가 TODO: ContentType 에 따른 처리 로직 필요
    questionPost.increaseCommentCount();
    questionPostRepository.save(questionPost);


    // 댓글 생성 저장
    Comment comment = commentRepository.save(Comment.builder()
        .member(member)
        .content(command.getContent())
        .postId(command.getPostId())
        .contentType(command.getContentType())
        .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
        .build());

    //FIXME: 임시 로깅
    lineLog(null);
    superLog(command);
    lineLog(null);

    return CommentDto.builder()
        .comment(comment)
        .build();
  }

  /**
   * <h3>특정 글에 작성된 모든 댓글 조회 로직
   * <p>
   * @param command
   * <p>postId: 특정 글 PK</p>
   * <p>contentType: 글 Type</p>
   * <p>pageNumber: n번째 페이지</p>
   * <p>pageSize: n개의 데이터</p>
   *
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
