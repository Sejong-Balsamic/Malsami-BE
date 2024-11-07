package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final ExpService expService;

  /**
   * <h3>질문 글 등록 로직
   * <p>TODO: 질문 글 작성시 엽전 100냥 감소 로직 작성
   * @param command
   * <p>String title
   * <p>String content
   * <p>String subject
   * <p>List mediaFiles
   * <p>List questionPresetTags
   * <p>List customTags
   * <p>Integer reward
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
      throw new CustomException(ErrorCode.QUESTION_REWARD_INVALID);
    }

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
        .dailyScore(0)
        .weeklyScore(0)
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
    }

    // 질문 글 등록 시 경험치 증가
    expService.updateMemberExp(member, ExpAction.CREATE_ANSWER_POST);

    return QuestionDto.builder()
        .questionPost(savedPost)
        .mediaFiles(mediaFiles)
        .customTags(customTags)
        .build();
  }

  /* 특정 질문 글 조회 로직 (해당 글 조회 수 증가) */
  @Transactional
  public QuestionDto findQuestionPost(QuestionCommand command) {

    QuestionPost questionPost = questionPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
    questionPost.increaseViewCount();
    log.info("제목: {}, 조회수: {}", questionPost.getTitle(), questionPost.getViewCount());

    return QuestionDto.builder()
        .questionPost(questionPostRepository.save(questionPost))
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

    // TODO: 로그 추후 이쁘게 변경할 예정
    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    log.info("개수: {}", posts.getNumberOfElements());
    log.info("테스트 결과: {}", posts.getContent()
        .stream().map(QuestionPost::getTitle)
        .toList());
    log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    return QuestionDto.builder()
        .questionPostsPage(posts)
        .build();
  }
}
