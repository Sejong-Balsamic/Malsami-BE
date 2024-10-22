package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.YeopjeonDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.QuestionBoardLike;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
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
public class QuestionBoardLikeService {

  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonHistoryService yeopjeonHistoryService;

  // 좋아요 이벤트 발생 시
  // QuestionCommand에서 QuestionId, ContentType 받음
  @Transactional
  public QuestionBoardLike increaseLikeCount(QuestionCommand command) {

    UUID postId = command.getQuestionPostId();

    Member writer;
    QuestionPost questionPost;
    AnswerPost answerPost;

    // 해당 글 좋아요 증가
    if (command.getContentType().equals(ContentType.QUESTION)) {
      questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
      writer = questionPost.getMember();
      questionPost.increaseLikeCount();
    } else if (command.getContentType().equals(ContentType.ANSWER)) {
      answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
      writer = answerPost.getMember();
      answerPost.increaseLikeCount();
    } else {
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 좋아요 받은 사용자 엽전 개수 증가 - A
    yeopjeonService.updateYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);

    YeopjeonDto yeopjeonDto;
    try {
      // 엽전 히스토리 내역 추가(MongoDB) - B
      yeopjeonDto = yeopjeonHistoryService.saveYeopjeonHistory(writer, YeopjeonAction.RECEIVE_LIKE);
    } catch (Exception e) {
      log.error("엽전 히스토리 저장 실패 및 롤백: {}", e.getMessage());

      // 보상 로직: 엽전 수 롤백 - B 실패 시 A 롤백
      yeopjeonService.rollbackYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);
      throw new CustomException(ErrorCode.YEOPJEON_HISTORY_SAVE_ERROR);
    }

    // MongoDB에 좋아요 내역 저장 - C
    try {
      return questionBoardLikeRepository.save(QuestionBoardLike.builder()
          .memberId(command.getMemberId())
          .questionBoardId(command.getQuestionPostId())
          .contentType(command.getContentType())
          .build());
    } catch (Exception e) {
      log.error("좋아요 내역 저장 실패 및 롤백: {}", e.getMessage());

      // 보상 로직: 엽전 히스토리 내역 삭제, 엽전 수 롤백 - C 실패시 A, B 롤백
      yeopjeonService.rollbackYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);
      yeopjeonHistoryService.deleteYeopjeonHistory(yeopjeonDto.getYeopjeonHistory());
      throw new CustomException(ErrorCode.QUESTION_BOARD_LIKE_SAVE_ERROR);
    }
  }
}
