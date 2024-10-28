package com.balsamic.sejongmalsami.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
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
  private YeopjeonRepository yeopjeonRepository;

  @Mock
  private YeopjeonHistoryService yeopjeonHistoryService;

  @Mock
  private YeopjeonConfig yeopjeonConfig;

  @Mock
  private Member loginMember, writer;

  @Mock
  private QuestionPost questionPost;

  @Mock
  private AnswerPost answerPost;

  @Mock
  private QuestionCommand questionPostCommand, answerPostCommand;

  @Mock
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

    // 테스트용 답변 글 설정
    answerPost = AnswerPost.builder()
        .answerPostId(UUID.randomUUID())
        .questionPost(questionPost)
        .member(writer)
        .build();

    // 테스트용 quetionPostCommand 설정
    questionPostCommand = QuestionCommand.builder()
        .memberId(loginMember.getMemberId())  // 로그인한 사용자 ID로 설정
        .postId(questionPost.getQuestionPostId())
        .contentType(ContentType.QUESTION)
        .build();

    // 테스트용 answerPostCommand 설정
    answerPostCommand = QuestionCommand.builder()
        .memberId(loginMember.getMemberId()) // 로그인한 사용자 ID로 설정
        .postId(answerPost.getAnswerPostId())
        .questionPostId(questionPost.getQuestionPostId())
        .contentType(ContentType.ANSWER)
        .build();

    // 테스트용 Yeopjeon 설정
    yeopjeon = Yeopjeon.builder()
        .resultYeopjeon(0)
        .member(writer)
        .build();

    // Mock 설정
    when(memberRepository.findById(loginMember.getMemberId())).thenReturn(Optional.of(loginMember));
    when(questionPostRepository.findById(questionPost.getQuestionPostId())).thenReturn(Optional.of(questionPost));
    when(answerPostRepository.findById(answerPost.getAnswerPostId())).thenReturn(Optional.of(answerPost));
    when(yeopjeonRepository.findByMember(writer)).thenReturn(Optional.of(yeopjeon));
    when(yeopjeonConfig.getLikeReward()).thenReturn(5);
    doAnswer(invocation -> {
      yeopjeon.updateResultYeopjeon(yeopjeon.getResultYeopjeon() + yeopjeonConfig.getLikeReward());
      return null;
    }).when(yeopjeonService).updateMemberYeopjeon(writer, YeopjeonAction.RECEIVE_LIKE);
  }

  @Test
  void 질문글_좋아요_증가_성공() {
    // 좋아요 받기 전 사용자 엽전개수
    log.info("좋아요 받기 전 작성자 엽전개수 : {}", yeopjeon.getResultYeopjeon());

    log.info("현재 로그인 된 사용자 ID : {}", questionPostCommand.getMemberId());
    log.info("질문 글 작성한 사용자 ID : {}", questionPost.getMember().getMemberId());
    // 질문글 초기 좋아요 개수 확인
    log.info("초기 질문 글 좋아요 개수 = {}", questionPost.getLikeCount());
    assertThat(questionPost.getLikeCount()).isEqualTo(0);

    questionBoardLikeService.increaseLikeCount(questionPostCommand);

    // 좋아요 받은 후 사용자 엽전개수
    log.info("좋아요 받은 후 작성자 엽전개수 : {}", yeopjeon.getResultYeopjeon());

    // 좋아요를 누른 사용자 확인
    assertThat(questionPostCommand.getMemberId()).isEqualTo(loginMember.getMemberId());

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

    questionPostCommand = QuestionCommand.builder()
        .questionPostId(UUID.randomUUID())
        .memberId(loginMember.getMemberId())
        .postId(questionPost.getQuestionPostId())
        .contentType(ContentType.QUESTION)
        .build();

    // Mock 설정 재정의
    when(questionPostRepository.findById(questionPost.getQuestionPostId())).thenReturn(Optional.of(questionPost));

    log.info("현재 로그인 된 사용자 ID : {}", questionPostCommand.getMemberId());
    log.info("질문 글 작성한 사용자 ID : {}", questionPost.getMember().getMemberId());
    // 본인 질문글에 좋아요 누를 경우 CustomException 발생
    CustomException exception = assertThrows(CustomException.class, () -> {
      questionBoardLikeService.increaseLikeCount(questionPostCommand);
    });

    // 작성자 엽전 개수 확인
    log.info("글 작성자 현재 엽전 개수 : {}", yeopjeon.getResultYeopjeon());
    assertThat(yeopjeon.getResultYeopjeon()).isEqualTo(0);

    // 에러 코드 확인
    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SELF_ACTION_NOT_ALLOWED);
  }

  @Test
  void 답변_좋아요_증가_성공() {

  }

  @Test
  void 본인_답변글_좋아요_예외처리() {

  }
}