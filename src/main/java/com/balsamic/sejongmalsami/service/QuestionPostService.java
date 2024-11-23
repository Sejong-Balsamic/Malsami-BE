package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ChaetaekStatus;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.QuestionPostCustomTag;
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
import java.util.stream.Collectors;
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
  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonCalculator yeopjeonCalculator;
  private final ExpService expService;
  private final AnswerPostRepository answerPostRepository;
  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;

  /**
   * 질문 글 등록
   * 1. 회원 엽전 검증 (현상금 + 작성 비용)
   * 2. 교과목 별 단과대 설정
   * 3. 질문글 기본정보 저장
   * 4. 정적/커스텀 태그 처리
   * 5. 첨부파일 업로드 및 썸네일 처리
   * 6. 엽전 차감 (-100냥)
   * 7. 경험치 증가
   * @return 저장된 질문글, 미디어파일, 커스텀태그 정보
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

    // {질문글 등록 시 소모엽전 + 엽전 현상금} 보다 보유 엽전량이 적을 시 오류 발생
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);
    if (yeopjeon.getYeopjeon() < command.getRewardYeopjeon()
        + -yeopjeonCalculator.calculateYeopjeon(YeopjeonAction.CREATE_QUESTION_POST)) {
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
        .stream().map(Course::getFaculty)
        .collect(Collectors.toList());

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
        .chaetaekStatus(false)
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

    QuestionPost savedQuestionPost = questionPostRepository.save(questionPost);

    // 커스텀 태그 추가
    List<String> customTags = null;
    if (command.getCustomTagSet() != null) {
      customTags = questionPostCustomTagService
          .saveCustomTags(command.getCustomTagSet(), savedQuestionPost.getQuestionPostId());
    }

    // 첨부파일 파일 업로드 및 썸네일 저장 -> 저장된 미디어파일 리스트 반환
    List<MediaFile> savedMediaFiles = mediaFileService.handleMediaFiles(
        ContentType.QUESTION,
        savedQuestionPost.getQuestionPostId(),
        command.getAttachmentFiles());

    // QuestionPost 에 썸네일 지정 : 저장된 사진 중 첫번째 사진
    if(!savedMediaFiles.isEmpty()){
      String thumbnailUrl = savedMediaFiles.get(0).getThumbnailUrl();
      questionPost.setThumbnailUrl(thumbnailUrl);
    }

    // 질문 글 등록 시 엽전 100냥 감소
    yeopjeonService.updateYeopjeonAndSaveYeopjeonHistory(member, YeopjeonAction.CREATE_QUESTION_POST);

    // 질문 글 등록 시 경험치 증가
    expService.updateExpAndSaveExpHistory(member, ExpAction.CREATE_QUESTION_POST);

    return QuestionDto.builder()
        .questionPost(savedQuestionPost)
        .mediaFiles(savedMediaFiles)
        .customTags(customTags)
        .build();
  }

  /**
   * 특정 질문 글 조회
   * 1. 조회수 증가
   * 2. 좋아요 여부 확인
   * 3. 답변 목록 조회
   * 4. 커스텀 태그 조회
   * @return 질문글, 답변목록, 커스텀태그 정보
   */
  @Transactional
  public QuestionDto getQuestionPost(QuestionCommand command) {
    // 질문 게시글 조회
    QuestionPost questionPost = questionPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 조회수 증가
    questionPost.increaseViewCount();
    log.info("제목: {}, 조회수: {}", questionPost.getTitle(), questionPost.getViewCount());

    // 좋아요 누른 회원인지 확인
    Boolean isLiked = questionBoardLikeRepository
        .existsByQuestionBoardIdAndMemberId(command.getPostId(), command.getMemberId());

    questionPost.updateIsLiked(isLiked);

    // 변경사항 저장
    questionPostRepository.save(questionPost);

    // 답변 조회 (없으면 null 반환)
    List<AnswerPost> answerPosts = answerPostRepository
        .findAllByQuestionPost(questionPost).orElse(null);

    // 커스텀 태그 조회 (없으면 null 반환)
    List<String> customTags = null;
    if (questionPostCustomTagRepository.existsByQuestionPostId(command.getQuestionPostId())) {
      customTags = questionPostCustomTagRepository
          .findAllByQuestionPostId(command.getPostId())
          .stream().map(QuestionPostCustomTag::getCustomTag)
          .collect(Collectors.toList());
    }

    return QuestionDto.builder()
        .questionPost(questionPost)
        .answerPosts(answerPosts)
        .customTags(customTags)
        .build();
  }

  /**
   * 전체 질문 글 페이징 조회
   * - 최신순 정렬 (createdDate DESC)
   * @return 질문글 페이지 정보
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
   * 미답변 질문글 조회 + 단과대 필터링
   * - 최신순 정렬 (createdDate DESC)
   * @return 질문글 페이지 정보
   */
  @Transactional(readOnly = true)
  public QuestionDto findAllQuestionPostsNotAnswered(QuestionCommand command) {

    Pageable pageable = PageRequest.of(command.getPageNumber(),
        command.getPageSize(),
        Sort.by("createdDate").descending());

    Page<QuestionPost> postPage = questionPostRepository
        .findNotAnsweredQuestionByFilter(command.getFaculty(), pageable);

    return QuestionDto.builder()
        .questionPostsPage(postPage)
        .build();
  }

  /**
   * 메인 필터링/정렬 조회
   * [필터링]
   * 1. 교과목명
   * 2. 정적태그 (최대 2개)
   * 3. 단과대
   * 4. 채택상태 (전체/채택/미채택)
   *
   * [정렬]
   * - 최신순(default)
   * - 좋아요순
   * - 현상금순
   * - 조회순
   * @return 필터링/정렬된 질문글 페이지
   */
  @Transactional(readOnly = true)
  public QuestionDto filteredQuestions(QuestionCommand command) {

    // 과목명이 비어있는 경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getSubject() != null && command.getSubject().isEmpty()) {
      command.setSubject(null);
    }

    // 정적태그 List 사이즈가 0인 경우 null로 설정 (비어있는 list의 경우 쿼리문에서 오류 발생)
    if (command.getQuestionPresetTags() != null && command.getQuestionPresetTags().isEmpty()) {
      command.setQuestionPresetTags(null);
    }

    // ChaetaekStatus 기본값 ALL 처리
    ChaetaekStatus chaetaekStatus;
    if (command.getChaetaekStatus() == null) {
      chaetaekStatus = ChaetaekStatus.ALL;
      log.info("chaetaekStatus가 null이므로 기본값인 ALL로 설정합니다.");
    } else {
      chaetaekStatus = command.getChaetaekStatus();
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

    // chaetaekStatus를 String으로 변환하여 전달
    Page<QuestionPost> posts = questionPostRepository.findQuestionPostsByFilter(
        command.getSubject(),
        command.getFaculty(),
        command.getQuestionPresetTags(),
        chaetaekStatus.name(), // Enum을 String으로 변환하여 전달
        pageable
    );

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


}
