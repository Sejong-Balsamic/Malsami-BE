package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerPostService {

  private final AnswerPostRepository answerPostRepository;
  private final MemberRepository memberRepository;
  private final QuestionPostRepository questionPostRepository;
  private final MediaFileService mediaFileService;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonHistoryService yeopjeonHistoryService;
  private final ExpService expService;
  private final ExpHistoryService expHistoryService;

  /**
   * <h3>답변 작성 로직
   * <p>작성자 경험치 증가
   * @param command: memberId, questionPostId, content, mediaFiles, isPrivate
   * @return 작성된 답변글, 첨부파일(이미지)
   */
  @Transactional
  public QuestionDto saveAnswer(QuestionCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    QuestionPost questionPost = questionPostRepository.findById(command.getQuestionPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 본인이 작성한 질문글에는 답변 작성 불가능
    if (member.equals(questionPost.getMember())) {
      throw new CustomException(ErrorCode.SELF_ANSWER_NOT_ALLOWED);
    }

    AnswerPost answerPost = answerPostRepository.save(AnswerPost.builder()
        .member(member)
        .questionPost(questionPost)
        .content(command.getContent())
        .likeCount(0)
        .commentCount(0)
        .isPrivate(Boolean.TRUE.equals(command.getIsPrivate()))
        .isChaetaek(false)
        .build());

    // 첨부파일 추가
    List<MediaFile> mediaFiles = null;
    if (command.getMediaFiles() != null) {
      mediaFiles = mediaFileService
          .uploadMediaFiles(answerPost.getAnswerPostId(), command.getMediaFiles());
    }

    // 답변이 작성된 질문 글 답변 수 증가
    questionPost.updateAnswerCount(answerPostRepository.countByQuestionPost(questionPost));
    log.info("{} 질문 글에 작성된 답변 수 : {}", questionPost.getQuestionPostId(), questionPost.getAnswerCount());

    // 답변 작성 시 경험치 증가 및 경험치 히스토리 내역 추가
    expService.updateExpAndSaveExpHistory(member, ExpAction.CREATE_COMMENT);

    return QuestionDto.builder()
        .answerPost(answerPost)
        .mediaFiles(mediaFiles)
        .build();
  }

  /**
   * <h3>답변 채택 로직
   * <p>1. 해당 답변글 isChaetaek true로 변경
   * <p>2. 글 작성자 - 엽전, 경험치 증가 및 내역 저장
   * <p>3. 사용자 - 엽전, 경험치 증가 및 내역 저장
   *
   * @param command: memberId(현재 접속중인 사용자), postId(답변 PK)
   * @return 채택 된 답변글
   */
  @Transactional
  public QuestionDto chaetaekAnswer(QuestionCommand command) {

    // 로그인된 사용자
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    AnswerPost answerPost = answerPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));

    QuestionPost questionPost = questionPostRepository.findQuestionPostByAnswerPost(answerPost)
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 답변 글 작성자
    Member writer = answerPost.getMember();

    // 채택 가능 여부 판단
    validateChaetaekConditions(questionPost, answerPost, command);

    // 엽전 변동 로직
    yeopjeonService.updateYeopjeonAndSaveYeopjeonHistory(writer, YeopjeonAction.CHAETAEK_CHOSEN);
    yeopjeonService.updateYeopjeonAndSaveYeopjeonHistory(member, YeopjeonAction.CHAETAEK_ACCEPT);

    // 경험치 변동 로직
    expService.updateExpAndSaveExpHistory(writer, ExpAction.CHAETAEK_CHOSEN);
    expService.updateExpAndSaveExpHistory(member, ExpAction.CHAETAEK_ACCEPT);
    
    // 답변 채택
    answerPost.chaetaekAnswer();
    log.info("답변글 : {} 채택되었습니다.", answerPost.getAnswerPostId());

    // 답변 글 채택여부 true 변경 후 리턴
    return QuestionDto.builder()
        .answerPost(answerPostRepository.save(answerPost))
        .build();
  }

  private void validateChaetaekConditions(QuestionPost questionPost, AnswerPost answerPost, QuestionCommand command) {

    // 로그인한 사용자
    Member member = questionPost.getMember();
    // 답변 글 작성자
    Member writer = answerPost.getMember();

    // 질문 작성자와 답변자가 같은 경우 채택불가
    if (member.getMemberId().equals(writer.getMemberId())) {
      log.error("본인이 작성한 글을 채택할 수 없습니다. 로그인된 사용자: {}, 글 작성자: {}",
          member.getStudentId(), writer.getStudentId());
      throw new CustomException(ErrorCode.SELF_CHAETAEK_NOT_ALLOWED);
    }

    // 질문 작성자만 답변 채택 가능 (로그인 된 사용자와 질문 작성자가 같은지 확인)
    if (!questionPost.getMember().getMemberId().equals(command.getMemberId())) {
      log.error("로그인한 사용자: {}, 질문 글 작성자: {}",
          member.getStudentId(), questionPost.getMember().getStudentId());
      throw new CustomException(ErrorCode.ONLY_AUTHOR_CAN_CHAETAEK);
    }

    // 해당 질문글의 답변 중 이미 채택된 답변이 있는 경우 채택 불가
    List<AnswerPost> answerPosts = answerPostRepository
        .findAnswerPostsByQuestionPost(answerPost.getQuestionPost());

    for (AnswerPost post : answerPosts) {
      if (post.getIsChaetaek().equals(Boolean.TRUE)) {
        throw new CustomException(ErrorCode.CHAETAEK_ANSWER_ALREADY_EXISTS);
      }
    }
  }
}
