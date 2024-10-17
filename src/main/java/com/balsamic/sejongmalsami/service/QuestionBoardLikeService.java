package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.QuestionBoardLike;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 누를경우
 * 누른 사용자 : 엽전, 경험치 증가
 * 좋아요 받은 사용자 : 엽전, 경험치 증가
 * 엽전 히스토리 내역 저장
 * 해당 질문글, 답변글 좋아요 수 증가
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionBoardLikeService {

  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final MemberRepository memberRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonHistoryService yeopjeonHistoryService;

  // 좋아요 이벤트 발생 시
  @Transactional
  public QuestionBoardLike increaseLikeCount(QuestionCommand command) {

    UUID postId = command.getQuestionPostId();

    // 좋아요 누른 사용자
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    ContentType contentType;
    Member writer;
    QuestionPost questionPost = null;
    AnswerPost answerPost = null;
    if (questionPostRepository.existsById(postId)) {
      contentType = ContentType.QUESTION;
      questionPost = questionPostRepository.getReferenceById(postId);
      writer = questionPost.getMember();
    } else if (answerPostRepository.existsById(postId)) {
      contentType = ContentType.ANSWER;
      answerPost = answerPostRepository.getReferenceById(postId);
      writer = answerPost.getMember();
    } else {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    // 좋아요 누른 사용자 엽전 개수 증가
    yeopjeonService.updateYeopjeon(member, YeopjeonAction.SEND_LIKE);

    // 좋아요 받은 사용자 엽전 개수 증가
    yeopjeonService.updateYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);

    // 해당 글의 좋아요 수 증가
    if (contentType.equals(ContentType.QUESTION)) {
      questionPost.increaseLikeCount();
    } else if (contentType.equals(ContentType.ANSWER)) {
      answerPost.increaseLikeCount();
    }

    // 엽전 히스토리 내역 추가
    yeopjeonHistoryService.addYeopjeonHistory(member, YeopjeonAction.SEND_LIKE);
    yeopjeonHistoryService.addYeopjeonHistory(writer, YeopjeonAction.RECEIVE_LIKE);

    // 좋아요 엔티티 저장
    return questionBoardLikeRepository.save(QuestionBoardLike.builder()
        .memberId(command.getMemberId())
        .questionBoardId(command.getQuestionPostId())
        .contentType(contentType)
        .build());
  }
}
