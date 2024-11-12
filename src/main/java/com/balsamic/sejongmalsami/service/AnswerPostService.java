package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.ExpHistory;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
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
  private final ExpService expService;

  /**
   * <h3>답변 작성 로직
   * <p>작성자 경험치 증가
   *
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
   * <p>4. 엽전현상금 - 질문 작성자 -> 답변 작성자 전달
   *
   * @param command: memberId(현재 접속중인 사용자), postId(답변 PK)
   * @return 채택 된 답변글
   */
  @Transactional
  public QuestionDto chaetaekAnswer(QuestionCommand command) {

    // 로그인된 사용자
    Member curMember = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    AnswerPost answerPost = answerPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));

    QuestionPost questionPost = questionPostRepository.findQuestionPostByAnswerPost(answerPost)
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 답변 글 작성자
    Member answerMember = answerPost.getMember();

    // 채택 가능 여부 판단
    validateChaetaekConditions(questionPost, answerPost, command);

    // 답변 작성자 엽전 변동 및 엽전 히스토리 저장 - A
    YeopjeonHistory answerMemberYeopjeonHistory = yeopjeonService
        .updateYeopjeonAndSaveYeopjeonHistory(answerMember, YeopjeonAction.CHAETAEK_CHOSEN);

    YeopjeonHistory curMemberYeopjeonHistory = null;
    try { // 로그인 사용자 엽전 변동 및 엽전 히스토리 저장 - B
      curMemberYeopjeonHistory = yeopjeonService
          .updateYeopjeonAndSaveYeopjeonHistory(curMember, YeopjeonAction.CHAETAEK_ACCEPT);
    } catch (Exception e) { // B 실패시 A 롤백
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          answerMember,
          YeopjeonAction.CHAETAEK_CHOSEN,
          answerMemberYeopjeonHistory
      );
    }

    // 경험치 변동 로직
    ExpHistory answerMemberExpHistory = null;
    try { // 답변 작성자 경험치 변동 및 경험치 히스토리 저장 - C
      answerMemberExpHistory = expService
          .updateExpAndSaveExpHistory(answerMember, ExpAction.CHAETAEK_CHOSEN);
    } catch (Exception e) { // C 실패시 A, B 롤백
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          curMember,
          YeopjeonAction.CHAETAEK_ACCEPT,
          curMemberYeopjeonHistory
      );
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          answerMember,
          YeopjeonAction.CHAETAEK_CHOSEN,
          answerMemberYeopjeonHistory
      );
    }
    ExpHistory curMemberExpHistory = null;
    try { // 로그인 사용자 경험치 변동 및 경험치 히스토리 저장 - D
      curMemberExpHistory = expService.updateExpAndSaveExpHistory(curMember, ExpAction.CHAETAEK_ACCEPT);
    } catch (Exception e) { // D 실패시 A, B, C 롤백
      expService.rollbackExpAndDeleteExpHistory(
          answerMember,
          ExpAction.CHAETAEK_CHOSEN,
          answerMemberExpHistory
      );
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          curMember,
          YeopjeonAction.CHAETAEK_ACCEPT,
          curMemberYeopjeonHistory
      );
      yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
          answerMember,
          YeopjeonAction.CHAETAEK_CHOSEN,
          answerMemberYeopjeonHistory
      );
    }

    // 답변 채택
    answerPost.chaetaekAnswer();
    log.info("답변글: {} 채택되었습니다. 해당 답변글 작성자: {}",
        answerPost.getAnswerPostId(), answerMember.getStudentId());

    // 엽전 현상금 존재 시 질문글 작성자 -> 답변글 작성자 엽전 전달
    if (questionPost.getRewardYeopjeon() > 0) {
      log.info("엽전 현상금이 존재합니다. 엽전 현상금: {}", questionPost.getRewardYeopjeon());
      Yeopjeon answerMemberYeopjeon = yeopjeonService.findMemberYeopjeon(answerMember);

      log.info("사용자: {} 의 엽전 현상금 지급 전 엽전량: {}",
          answerMember.getStudentId(), answerMemberYeopjeon.getYeopjeon());
      try { // 답변 작성자 엽전 변동 및 엽전 히스토리 저장 - E
        yeopjeonService.updateYeopjeonAndSaveYeopjeonHistory(
            answerMember,
            YeopjeonAction.REWARD_YEOPJEON,
            questionPost.getRewardYeopjeon());
      } catch (Exception e) { // E 실패시 A, B, C, D 롤백
        expService.rollbackExpAndDeleteExpHistory(
            curMember,
            ExpAction.CHAETAEK_ACCEPT,
            curMemberExpHistory
        );
        expService.rollbackExpAndDeleteExpHistory(
            answerMember,
            ExpAction.CHAETAEK_CHOSEN,
            answerMemberExpHistory
        );
        yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
            curMember,
            YeopjeonAction.CHAETAEK_ACCEPT,
            curMemberYeopjeonHistory
        );
        yeopjeonService.rollbackYeopjeonAndDeleteYeopjeonHistory(
            answerMember,
            YeopjeonAction.CHAETAEK_CHOSEN,
            answerMemberYeopjeonHistory
        );
      }

      log.info("사용자: {} 의 엽전 현상금 지급 후 엽전량: {}",
          answerMember.getStudentId(), answerMemberYeopjeon.getYeopjeon());

    }

    // 답변 글 채택여부 true 변경
    // 변경사항 저장 및 반환
    return QuestionDto.builder()
        .answerPost(answerPostRepository.save(answerPost))
        .build();
  }

  // 채택 가능 여부 검증 메서드
  private void validateChaetaekConditions(QuestionPost questionPost, AnswerPost answerPost, QuestionCommand command) {

    // 로그인한 사용자
    Member curMember = questionPost.getMember();
    // 답변 글 작성자
    Member answerWriter = answerPost.getMember();

    // 질문 작성자와 답변자가 같은 경우 채택불가
    if (curMember.getMemberId().equals(answerWriter.getMemberId())) {
      log.error("본인이 작성한 글을 채택할 수 없습니다. 로그인된 사용자: {}, 글 작성자: {}",
          curMember.getStudentId(), answerWriter.getStudentId());
      throw new CustomException(ErrorCode.SELF_CHAETAEK_NOT_ALLOWED);
    }

    // 질문 작성자만 답변 채택 가능 (로그인 된 사용자와 질문 작성자가 같은지 확인)
    if (!questionPost.getMember().getMemberId().equals(command.getMemberId())) {
      log.error("로그인한 사용자: {}, 질문 글 작성자: {}",
          curMember.getStudentId(), questionPost.getMember().getStudentId());
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
