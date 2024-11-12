package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.CommentLike;
import com.balsamic.sejongmalsami.object.mongo.ExpHistory;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.mongo.CommentLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
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
public class CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final MemberRepository memberRepository;
  private final YeopjeonService yeopjeonService;
  private final ExpService expService;

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
    if (curMember.equals(commentWriter)) {
      log.error("본인이 작성한 댓글에 좋아요 불가. 로그인한 사용자: {}, 작성자: {}",
          curMember.getStudentId(), commentWriter.getStudentId());
      throw new CustomException(ErrorCode.SELF_LIKE_NOT_ALLOWED);
    }

    // 이미 좋아요를 누른 경우 검증
    if (commentLikeRepository.existsByCommentIdAndMemberId(postId, curMember.getMemberId())) {
      log.error("이미 좋아요를 누른 댓글입니다. CommentId: {}, 로그인한 사용자: {}",
          postId, curMember.getStudentId());
      throw new CustomException(ErrorCode.ALREADY_LIKED);
    }

    // 좋아요 받은 사용자 엽전 개수 증가 및 엽전 히스토리 저장 - A
    YeopjeonHistory writerYeopjeonHistory = yeopjeonService
        .updateYeopjeonAndSaveYeopjeonHistory(commentWriter, YeopjeonAction.RECEIVE_LIKE);

    // 좋아요 받은 사용자 경험치 증가 및 경험치 히스토리 저장 - B
    ExpHistory writerExpHistory = null;
    try {
      writerExpHistory = expService
          .updateExpAndSaveExpHistory(commentWriter, ExpAction.RECEIVE_LIKE);
    } catch (Exception e) {
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          commentWriter,
          YeopjeonAction.RECEIVE_LIKE,
          writerYeopjeonHistory
      );
    }

    // 좋아요 증가 및 MongoDB에 좋아요 내역 저장 - C
    try {
      // 좋아요 증가, contentType 댓글 고정
      comment.increaseLikeCount();
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

      // 엽전, 경험치 및 히스토리 롤백 - C 실패시 A, B 롤백
      expService.rollbackExpAndDeleteExpHistory(
          commentWriter, ExpAction.RECEIVE_LIKE, writerExpHistory);
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          commentWriter,
          YeopjeonAction.RECEIVE_LIKE,
          writerYeopjeonHistory
      );
      throw new CustomException(ErrorCode.COMMENT_LIKE_HISTORY_SAVE_ERROR);
    }
  }
}
