package com.balsamic.sejongmalsami.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@Slf4j
class QuestionBoardLikeServiceTest {

  @InjectMocks
  private QuestionBoardLikeService questionBoardLikeService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private QuestionBoardLikeRepository questionBoardLikeRepository;

  @Mock
  private QuestionPostRepository questionPostRepository;

  @Mock
  private AnswerPostRepository answerPostRepository;

  @Mock
  private YeopjeonService yeopjeonService;

  @Mock
  private YeopjeonHistoryService yeopjeonHistoryService;

  private Member loginMember, writer;
  private QuestionPost questionPost;
  private AnswerPost answerPost;
  private QuestionCommand command;
  private Yeopjeon yeopjeon;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);

    // 현재 사용자
    loginMember = Member.builder().memberId(UUID.randomUUID()).studentId(11111111L).build();

    // 글 작성자
    writer = Member.builder().memberId(UUID.randomUUID()).studentId(22222222L).build();

    // 테스트용 질문 게시글 설정
    questionPost = QuestionPost.builder()
        .questionPostId(UUID.randomUUID())
        .member(writer)
        .build();

    // 테스트용 command 설정
    command = QuestionCommand.builder()
        .memberId(loginMember.getMemberId())  // 로그인한 사용자 ID로 설정
        .postId(questionPost.getQuestionPostId())
        .contentType(ContentType.QUESTION)
        .build();

    // 테스트용 Yeopjeon 설정


    // Mock 설정
    when(memberRepository.findById(loginMember.getMemberId())).thenReturn(Optional.of(loginMember));
    when(questionPostRepository.findById(questionPost.getQuestionPostId())).thenReturn(Optional.of(questionPost));
  }

  @Test
  void 질문글_좋아요_증가_성공() {

    log.info("현재 로그인 된 사용자 ID : {}", command.getMemberId());
    log.info("질문 글 작성한 사용자 ID : {}", questionPost.getMember().getMemberId());
    // 질문글 초기 좋아요 개수 확인
    log.info("초기 질문 글 좋아요 개수 = {}", questionPost.getLikeCount());
    assertThat(questionPost.getLikeCount()).isEqualTo(0);

    questionBoardLikeService.increaseLikeCount(command);

    // 좋아요를 누른 사용자 확인
    assertThat(command.getMemberId()).isEqualTo(loginMember.getMemberId());

    // 글 작성자 확인
    assertThat(writer).isEqualTo(questionPost.getMember());

    // 좋아요 누른 뒤 좋아요 개수 확인
    log.info("좋아요 누른 뒤 해당 질문글 좋아요 개수 : {}", questionPost.getLikeCount());
    assertThat(questionPost.getLikeCount()).isEqualTo(1);
  }

  @Test
  void 본인_질문글_좋아요_예외처리() {

    // 로그인 된 사용자가 작성한 글
    questionPost = QuestionPost.builder()
        .questionPostId(UUID.randomUUID())
        .member(loginMember)
        .build();

    command = QuestionCommand.builder()
        .questionPostId(UUID.randomUUID())
        .memberId(loginMember.getMemberId())
        .postId(questionPost.getQuestionPostId())
        .contentType(ContentType.QUESTION)
        .build();

    // Mock 설정 재정의
    when(questionPostRepository.findById(questionPost.getQuestionPostId())).thenReturn(Optional.of(questionPost));

    log.info("현재 로그인 된 사용자 ID : {}", command.getMemberId());
    log.info("질문 글 작성한 사용자 ID : {}", questionPost.getMember().getMemberId());
    // 본인 질문글에 좋아요 누를 경우 CustomException 발생
    CustomException exception = assertThrows(CustomException.class, () -> {
      questionBoardLikeService.increaseLikeCount(command);
    });

    // 에러 코드 확인
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SELF_ACTION_NOT_ALLOWED);
  }

//  @Test
//  void 답변_좋아요_증가_성공() {
//
//  }
//
//  @Test
//  void 본인_답변글_좋아요_예외처리() {
//
//  }
//
//  @Test
//  void 좋아요_받은_사용자_엽전개수_증가() {
//    log.info("현재 로그인 된 사용자 ID : {}", command.getMemberId());
//    log.info("질문 글 작성한 사용자 ID : {}", questionPost.getMember().getMemberId());
//
//    // loginMember가 writer가 작성한 글에 좋아요 누름
//    questionBoardLikeService.increaseLikeCount(command);
//
//    // 좋아요를 누른 사용자 확인
//    assertThat(command.getMemberId()).isEqualTo(loginMember.getMemberId());
//
//    // 글 작성자 확인
//    assertThat(writer).isEqualTo(questionPost.getMember());
//
//
//  }
}