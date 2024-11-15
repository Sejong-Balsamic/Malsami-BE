package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.YeopjeonCalculator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionPostService {

  private final QuestionPostRepository questionPostRepository;
  private final MemberRepository memberRepository;
  private final QuestionPostCustomTagService questionPostCustomTagService;
  private final MediaFileService mediaFileService;
  private final CourseRepository courseRepository;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonCalculator yeopjeonCalculator;
  private final ExpService expService;
  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;
  private final QuestionBoardLikeRepository questionBoardLikeRepository;


  //FIXME: 임시 사용 : MOCK CUSTOM TAGS 생성
  private final Faker faker = new Faker(new Locale("ko"));
  private final AnswerPostRepository answerPostRepository;

  /**
   * <h3>질문 글 등록 로직
   * <p>작성자 엽전 100냥 감소
   * <p>작성자 경험치 증가
   * @param command
   * <p>String title
   * <p>String content
   * <p>String subject
   * <p>List mediaFiles
   * <p>List questionPresetTags
   * <p>List customTags
   * <p>Integer rewardYeopjeon
   * <p>Boolean isPrivate
   * @return
   */
  @Transactional
  public QuestionDto saveQuestionPost(QuestionCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 엽전 현상금 null인 경우 기본 0으로 설정
    if (command.getRewardYeopjeon() == null) {
      command.setRewardYeopjeon(0);
    } else if (command.getRewardYeopjeon() < 0) { // 음수 값으로 설정될 경우 오류
      throw new CustomException(ErrorCode.QUESTION_INVALID_REWARD_YEOPJEON);
    }

    // {질문글 등록 시 소모엽전 + 엽전 현싱금} 보다 보유 엽전량이 적을 시 오류 발생
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);
    if (yeopjeon.getYeopjeon() < command.getRewardYeopjeon() + -(yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CREATE_QUESTION_POST))) {
      log.error("사용자: {} 의 엽전이 부족합니다.", member.getStudentId());
      log.error("현재 보유 엽전량: {}, 질문글 등록시 필요 엽전량: {}, 엽전 현상금 설정량: {}",
          yeopjeon.getYeopjeon(),
          -yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CREATE_QUESTION_POST),
          command.getRewardYeopjeon());
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    }
    // 질문글 작성자가 등록한 엽전 현상금 만큼 엽전 수 감소
    yeopjeonService.updateYeopjeonAndSaveYeopjeonHistory(
        member,
        YeopjeonAction.REWARD_YEOPJEON,
        -command.getRewardYeopjeon());

    // 입력된 교과목에 따른 단과대 설정
    List<Faculty> faculties = courseRepository
        .findAllBySubject(command.getSubject())
        .stream().map(Course::getFaculty).toList();

    log.info("입력된 교과목명 : {}", command.getSubject());
    log.info("단과대 List : {}", faculties);

    if (faculties.isEmpty()) {
      throw new CustomException(ErrorCode.FACULTY_NOT_FOUND);
    }

    QuestionPost questionPost = QuestionPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .faculties(faculties)
        .questionPresetTags(new ArrayList<>())
        .viewCount(0)
        .likeCount(0)
        .answerCount(0)
        .commentCount(0)
        .rewardYeopjeon(command.getRewardYeopjeon())
        .dailyScore(0L)
        .weeklyScore(0L)
        .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
        .build();

    // 정적 태그 추가
    if (command.getQuestionPresetTags() != null) {
      for (QuestionPresetTag tag : command.getQuestionPresetTags()) {
        questionPost.addPresetTag(tag);
      }
    }

    QuestionPost savedPost = questionPostRepository.save(questionPost);

    // 커스텀 태그 추가
    List<String> customTags = null;
    if (command.getCustomTagSet() != null) {
      customTags = questionPostCustomTagService
          .saveCustomTags(command.getCustomTagSet(), savedPost.getQuestionPostId());
    }

    // 첨부파일 추가
    List<MediaFile> mediaFiles = null;
    if (command.getMediaFiles() != null) {
      mediaFiles = mediaFileService
          .uploadMediaFiles(savedPost.getQuestionPostId(), command.getMediaFiles());

      // 첫번째 이미지를 썸네일로 설정
      questionPost.addThumbnail(mediaFiles.get(0).getFileUrl());
    }

    // 질문 글 등록 시 엽전 100냥 감소
    yeopjeonService.updateYeopjeonAndSaveYeopjeonHistory(member, YeopjeonAction.CREATE_QUESTION_POST);

    // 질문 글 등록 시 경험치 증가
    expService.updateExpAndSaveExpHistory(member, ExpAction.CREATE_QUESTION_POST);

    return QuestionDto.builder()
        .questionPost(savedPost)
        .mediaFiles(mediaFiles)
        .customTags(customTags)
        .build();
  }

  /* 특정 질문 글 조회 로직 (해당 글 조회 수 증가) */
  @Transactional
  public QuestionDto getQuestionPost(QuestionCommand command) {

    // 질문 게시글 조회
    QuestionPost questionPost = questionPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 조회수 증가
    questionPost.increaseViewCount();
    log.info("제목: {}, 조회수: {}", questionPost.getTitle(), questionPost.getViewCount());

    // 변경사항 저장
    questionPostRepository.save(questionPost);

    // 답변 조회 (없으면 null 반환)
    List<AnswerPost> answerPost = answerPostRepository.findAllByQuestionPost(questionPost).orElse(null);

    //FIXME : 임시 커스텀 태그 생성 : DB 에서 불러와야합니다
    List<String> customTags = new ArrayList<>();
    int tagCount = faker.number().numberBetween(1, 5);

    for (int i = 0; i < tagCount; i++) {
      // 특수문자를 제거한 후, 10자 이하인 문장만 추가
      String sentence = faker.lorem().sentence().substring(0,10);
      String cleanedSentence = sentence.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", "").trim();
      customTags.add(cleanedSentence);
    }
    //TODO: 커스텀 태그 조회

    // 좋아요 누른 회원인지 확인


    return QuestionDto.builder()
        .questionPost(questionPost)
        .answerPosts(answerPost)
        .customTags(customTags)
        .build();
  }

  /**
   * 전체 질문 글 조회 (최신순)
   * @param command <br>
   * Integer pageNumber <br>
   * Integer PageSize <br>
   *
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto findAllQuestionPost(QuestionCommand command) {

    Pageable pageable = PageRequest.of(command.getPageNumber(),
        command.getPageSize(),
        Sort.by("createdDate").descending());

    Page<QuestionPost> posts = questionPostRepository.findAll(pageable);

    return QuestionDto.builder()
        .questionPostsPage(posts)
        .build();
  }

  /**
   * 아직 답변 안된 글 조회 로직 + 단과대 필터링 (정렬: 최신순)
   * @param command
   * <p>Faculty faculty
   * <p>Integer pageNumber
   * <p>Integer pageSize
   *
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto findAllQuestionPostsNotAnswered(QuestionCommand command) {

    Pageable pageable = PageRequest.of(command.getPageNumber(),
        command.getPageSize(),
        Sort.by("createdDate").descending());

    Page<QuestionPost> postPage = questionPostRepository
        .findFilteredNotAnsweredQuestion(command.getFaculty(), pageable);

    return QuestionDto.builder()
        .questionPostsPage(postPage)
        .build();
  }

  /**
   * <h3>질문글 필터링 로직
   * <p>1. 교과목명 기준 필터링 - String subject (ex. 컴퓨터구조, 인터렉티브 디자인)
   * <p>2. 엽전 현상금 범위 필터링 - Integer minYeopjeon, Integer maxYeopjeon
   * <p>3. 정적 태그 필터링 - QuestionPresetTag (최대 2개)
   * <p>4. 단과대별 필터링 - Faculty (ex. 공과대학, 예체는대학)
   * <p>5. 아직 채택되지 않은 질문 글 필터링 - Boolean viewNotChaetaek
   * <br><br>
   * <h3>정렬 로직 (SortType)
   * <p>최신순, 좋아요순, 엽전 현상금순, 조회순
   *
   * @param command
   * <p>String subject
   * <p>Integer minYeopjeon
   * <p>Integer maxYeopjeon
   * <p>List<QuestionPresetTag> questionPresetTags
   * <p>Faculty
   * <p>Boolean viewNotChaetaek
   * <p>SortType
   *
   * @return Page questionPosts
   */
  @Transactional
  public QuestionDto filteredQuestions(QuestionCommand command) {

    // 과목명이 비어있는경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getSubject().isEmpty()) {
      command.setSubject(null);
    }

    // 정적태그 List 사이즈가 0인경우 null로 설정 (비어있는 list의 경우 쿼리문에서 오류 발생)
    if (command.getQuestionPresetTags().isEmpty()) {
      command.setQuestionPresetTags(null);
    }

    // 정렬 기준 (default: 최신순)
    SortType sortType = (command.getSortType() != null) ? command.getSortType() : SortType.LATEST;

    Sort sort;
    switch (sortType) {
      case LATEST -> sort = Sort.by(Direction.DESC, "createdDate");
      case MOST_LIKED -> sort = Sort.by(Direction.DESC, "likeCount");
      case YEOPJEON_REWARD -> sort = Sort.by(Direction.DESC, "rewardYeopjeon");
      case VIEW_COUNT -> sort = Sort.by(Direction.DESC, "viewCount");
      default -> sort = Sort.by(Direction.DESC, "createdDate");
    }

    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    Page<QuestionPost> posts = questionPostRepository
        .findFilteredQuestions(command.getSubject(),
            command.getMinYeopjeon(),
            command.getMaxYeopjeon(),
            command.getFaculty(),
            command.getQuestionPresetTags(),
            command.getViewNotChaetaek(),
            pageable);

    return QuestionDto.builder()
        .questionPostsPage(posts)
        .build();
  }

  //FIXME: 임시 SERVICE 코드 (관리자용)
  public QuestionDto getAllQuestions() {
    return QuestionDto.builder().build();
  }

  public QuestionDto getQuestionById(QuestionCommand command) {
    return QuestionDto.builder().build();
  }

  public void saveQuestion(QuestionCommand command) {
  }

  public void deleteQuestion(QuestionCommand command) {
  }

  private QuestionDto convertToDto(QuestionPost entity) {
    return QuestionDto.builder().build();
  }
}
