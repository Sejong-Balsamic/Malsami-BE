package com.balsamic.sejongmalsami.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.TestDataGenerator;
import com.balsamic.sejongmalsami.util.YeopjeonCalculator;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
@Transactional
class QuestionPostServiceTest {

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  QuestionPostRepository questionPostRepository;

  @Autowired
  QuestionPostService questionPostService;

  @Autowired
  AnswerPostService answerPostService;

  @Autowired
  CourseRepository courseRepository;

  @Autowired
  YeopjeonService yeopjeonService;

  @Autowired
  YeopjeonConfig yeopjeonConfig;

  @Autowired
  YeopjeonCalculator yeopjeonCalculator;

  @Autowired
  TestDataGenerator testDataGenerator;

  private static final String MEMBER_PK_BAEK = "fd168ebc-d1c6-4e8e-87f4-8ebec66973d0";
  private static final String MEMBER_PK_SUH = "094891c9-6d79-4fb5-bb80-be3c20b35767";
  private static final int REWARD_YEOPJEON = 30;

  @Test
  void 메인_테스트() {
//    질문_글_저장_성공();
//    질문_글_저장_실패();
//    특정_질문_글_조회();
//    아직_답변이_없는글_단과대_필터링();
//    과목_필터링();
//    답변_채택();
    엽전_현상금_초과();
  }

  @Test
  void 질문_Mock_데이터_저장() {
    for (int i = 0; i < 100; i++) {
      Member mockMember = testDataGenerator.createMockMember();
      testDataGenerator.createMockQuestionPost(mockMember);
    }
  }

  void 질문_글_저장_성공() {

    QuestionCommand command = new QuestionCommand();
    command.setMemberId(UUID.fromString(MEMBER_PK_BAEK));
    command.setTitle("테스트 제목입니다.");
    command.setContent("테스트 본문입니다.");
    command.setSubject("컴퓨터구조");

    QuestionDto dto = questionPostService.saveQuestionPost(command);

    log.info("dto PK: {}", dto.getQuestionPost().getQuestionPostId());

    QuestionPost findPost = questionPostRepository
        .findById(dto.getQuestionPost().getQuestionPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    log.info("작성 된 질문 글 제목: {}", findPost.getTitle());
    log.info("작성 된 질문 글 본문: {}", findPost.getContent());
    log.info("작성 된 질문 글 과목 명: {}", findPost.getSubject());
    log.info("작성 된 질문 글 단과대: {}", findPost.getFaculties());

//    assertThat(findPost.getFaculties()).contains()
  }

  void 질문_글_저장_실패() {
    QuestionPost post = QuestionPost.builder()
        .title("테스트 제목입니다.")
        .content("테스트 본문입니다.")
        .subject("실패해야하는 과목명입니다.")
        .build();

    Assertions.assertThrows(Exception.class, () ->
        questionPostRepository.save(post));
  }

  void 특정_질문_글_조회() {
    // 질문 글 저장
    QuestionCommand command = new QuestionCommand();
    command.setMemberId(UUID.fromString(MEMBER_PK_BAEK));
    command.setTitle("테스트 제목입니다.");
    command.setContent("테스트 본문입니다.");
    command.setSubject("컴퓨터구조");

    QuestionDto dto = questionPostService.saveQuestionPost(command);

    log.info("dto title: {}", dto.getQuestionPost().getTitle());

    QuestionPost findPost = questionPostRepository
        .findById(dto.getQuestionPost().getQuestionPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 특정 질문 글 조회
    log.info("요청 전 조회 수: {}", findPost.getViewCount());

    QuestionCommand newCommand = new QuestionCommand();
    newCommand.setPostId(dto.getQuestionPost().getQuestionPostId());

    questionPostService.findQuestionPost(newCommand);
    log.info("요청 후 조회 수: {}", findPost.getViewCount());
  }

  void 아직_답변이_없는글_단과대_필터링() {

    Faculty faculty = Faculty.대양휴머니티칼리지;

    Pageable pageable = PageRequest.of(0, 30, Sort.by("createdDate").descending());

    Page<QuestionPost> posts = questionPostRepository.findFilteredNotAnsweredQuestion(faculty, pageable);

    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    log.info("개수: {}", posts.getNumberOfElements());
    log.info("테스트 결과: {}", posts.getContent()
        .stream().map(QuestionPost::getTitle)
        .toList());
    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
  }

  void 과목_필터링() {

    String subject = null;
    Integer minYeopjeon = null;
    Integer maxYeopjeon = null;
    Faculty faculty = null;
    List<QuestionPresetTag> questionPresetTags = null;
//    questionPresetTags.add(QuestionPresetTag.DOCUMENT_REQUEST);
//    questionPresetTags.add(QuestionPresetTag.UNKNOWN_CONCEPT);
    Boolean viewNotChaetaek = true;

    SortType sortType = SortType.LATEST;

    Sort sort;
    switch (sortType) {
      case LATEST -> sort = Sort.by(Direction.DESC, "createdDate");
      case MOST_LIKED -> sort = Sort.by(Direction.DESC, "likeCount");
      case YEOPJEON_REWARD -> sort = Sort.by(Direction.DESC, "rewardYeopjeon");
      case VIEW_COUNT -> sort = Sort.by(Direction.DESC, "viewCount");
      default -> sort = Sort.by(Direction.DESC, "createdDate");
    }

    Pageable pageable = PageRequest.of(0, 30, sort);

    Page<QuestionPost> posts = questionPostRepository.findFilteredQuestions(
        subject,
        minYeopjeon,
        maxYeopjeon,
        faculty,
        questionPresetTags,
        viewNotChaetaek,
        pageable
    );

    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    log.info("개수: {}", posts.getNumberOfElements());
    log.info("테스트 결과: {}", posts.getContent()
        .stream().map(QuestionPost::getTitle)
        .toList());
    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
  }

  void 답변_채택() {
    // 질문 글 저장 - 작성자: member1
    Member member1 = memberRepository.findById(UUID.fromString(MEMBER_PK_BAEK))
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Yeopjeon member1Yeopjeon = yeopjeonService.findMemberYeopjeon(member1);

    log.info("글 작성 전 엽전 개수: {}", member1Yeopjeon.getYeopjeon());
    int member1InitYeopjeon = member1Yeopjeon.getYeopjeon();

    QuestionCommand questionCommand = new QuestionCommand();
    questionCommand.setMemberId(UUID.fromString(MEMBER_PK_BAEK));
    questionCommand.setTitle("테스트 제목입니다.");
    questionCommand.setContent("테스트 본문입니다.");
    questionCommand.setSubject("컴퓨터구조");
    questionCommand.setRewardYeopjeon(REWARD_YEOPJEON);

    QuestionDto saveQuestionPost = questionPostService.saveQuestionPost(questionCommand);

    log.info("글 작성 후 엽전 개수: {}", member1Yeopjeon.getYeopjeon());
    assertThat(member1Yeopjeon.getYeopjeon())
        .isEqualTo(
            member1InitYeopjeon
            + yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CREATE_QUESTION_POST)
            - yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.REWARD_YEOPJEON, REWARD_YEOPJEON));

    // 답변 글 저장 - 작성자: member2
    Member member2 = memberRepository.findById(UUID.fromString(MEMBER_PK_SUH))
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Yeopjeon member2Yeopjeon = yeopjeonService.findMemberYeopjeon(member2);

    log.info("member2 초기 엽전 개수: {}", member2Yeopjeon.getYeopjeon());
    int member2InitYeopjeon = member2Yeopjeon.getYeopjeon();

    QuestionCommand answerCommand = new QuestionCommand();
    answerCommand.setMemberId(member2.getMemberId());
    answerCommand.setQuestionPostId(saveQuestionPost.getQuestionPost().getQuestionPostId());
    answerCommand.setContent("테스트 답변글 입니다.");
    QuestionDto saveAnswer = answerPostService.saveAnswer(answerCommand);

    // 답변 채택
    QuestionCommand chaetaekCommand = new QuestionCommand();
    chaetaekCommand.setMemberId(UUID.fromString(MEMBER_PK_BAEK));
    chaetaekCommand.setPostId(saveAnswer.getAnswerPost().getAnswerPostId());
    answerPostService.chaetaekAnswer(chaetaekCommand);
    log.info("member2 답변 채택 된 후 엽전 개수: {}", member2Yeopjeon.getYeopjeon());
    assertThat(member2Yeopjeon.getYeopjeon())
        .isEqualTo(
            member2InitYeopjeon
            + yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CHAETAEK_CHOSEN)
            + yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.REWARD_YEOPJEON, REWARD_YEOPJEON));

    log.info("답변 채택 후 member1 엽전 개수: {}", member1Yeopjeon.getYeopjeon());
    assertThat(member1Yeopjeon.getYeopjeon())
        .isEqualTo(
            member1InitYeopjeon
            + yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CREATE_QUESTION_POST)
            - yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.REWARD_YEOPJEON, REWARD_YEOPJEON)
            + yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CHAETAEK_ACCEPT)
        );
  }

  void 엽전_현상금_초과() {
    // 질문 글 저장 - 작성자: member1
    Member member1 = memberRepository.findById(UUID.fromString(MEMBER_PK_BAEK))
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Yeopjeon member1Yeopjeon = yeopjeonService.findMemberYeopjeon(member1);

    log.info("글 작성 전 엽전 개수: {}", member1Yeopjeon.getYeopjeon());
    int member1InitYeopjeon = member1Yeopjeon.getYeopjeon();

    QuestionCommand questionCommand = new QuestionCommand();
    questionCommand.setMemberId(UUID.fromString(MEMBER_PK_BAEK));
    questionCommand.setTitle("테스트 제목입니다.");
    questionCommand.setContent("테스트 본문입니다.");
    questionCommand.setSubject("컴퓨터구조");
    questionCommand.setRewardYeopjeon(110);

    Assertions.assertThrows(CustomException.class, () ->
        questionPostService.saveQuestionPost(questionCommand));

    log.info("오류 발생 후 member1 엽전 개수: {}", member1Yeopjeon.getYeopjeon());
  }
}
