package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.SortType.LATEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.MOST_LIKED;
import static com.balsamic.sejongmalsami.object.constants.SortType.REWARD_YEOPJEON_DESCENDING;
import static com.balsamic.sejongmalsami.object.constants.SortType.REWARD_YEOPJEON_LATEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.VIEW_COUNT;
import static com.balsamic.sejongmalsami.object.constants.SortType.getJpqlSortOrder;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ChaetaekStatus;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.QuestionPostCustomTag;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.RedisLockManager;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionPostService {

  private final static long WAIT_TIME = 5L; // Lock을 얻기위해 기다리는 시간
  private final static long LEASE_TIME = 2L; // Lock 자동 해제 시간

  private final QuestionPostRepository questionPostRepository;
  private final MemberRepository memberRepository;
  private final QuestionPostCustomTagService questionPostCustomTagService;
  private final MediaFileService mediaFileService;
  private final CourseRepository courseRepository;
  private final QuestionBoardLikeRepository questionBoardLikeRepository;
  private final YeopjeonService yeopjeonService;
  private final ExpService expService;
  private final AnswerPostRepository answerPostRepository;
  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;
  private final RedisLockManager redisLockManager;
  private final PostEmbeddingService postEmbeddingService;

  /**
   * <h3>질문 글 등록</h3>
   * <ol>
   *   <li>회원 엽전 검증 (현상금 + 작성 비용)</li>
   *   <li>교과목 별 단과대 설정</li>
   *   <li>질문글 기본정보 저장</li>
   *   <li>정적/커스텀 태그 처리</li>
   *   <li>첨부파일 업로드 및 썸네일 처리</li>
   *   <li>엽전 차감(-100냥)</li>
   *   <li>경험치 증가</li>
   * </ol>
   *
   * @param command memberId, title, content, subject, attachmentFiles, questionPresetTags, customTags, rewardYeopjeon,
   *                isPrivate
   * @return 저장된 질문글, 첨부파일, 커스텀태그 정보
   */
  @Transactional
  public QuestionDto saveQuestionPost(QuestionCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 질문 글 등록시 필요한 엽전 검증
    yeopjeonService.validateYeopjeonForQuestionPost(member, command.getRewardYeopjeon());

    // 입력된 교과목에 따른 단과대 설정
    List<String> faculties = courseRepository.findAllBySubject(command.getSubject())
        .stream()
        .map(Course::getFaculty)
        .filter(faculty -> faculty != null && !faculty.trim().isEmpty())
        .distinct()
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
    if (command.getCustomTags() != null) {
      customTags = questionPostCustomTagService
          .saveCustomTags(command.getCustomTags(), savedQuestionPost.getQuestionPostId());
    }
    savedQuestionPost.setCustomTags(customTags);

    // 첨부파일 파일 업로드 및 썸네일 저장 -> 저장된 미디어파일 리스트 반환
    List<MediaFile> savedMediaFiles = mediaFileService.handleMediaFiles(
        ContentType.QUESTION,
        savedQuestionPost.getQuestionPostId(),
        command.getAttachmentFiles());

    // QuestionPost 에 썸네일 지정 : 저장된 사진 중 첫번째 사진
    if (!savedMediaFiles.isEmpty()) {
      String thumbnailUrl = savedMediaFiles.get(0).getThumbnailUrl();
      questionPost.setThumbnailUrl(thumbnailUrl);
    }

    // 벡터 생성 및 저장
    postEmbeddingService.saveEmbedding(
        savedQuestionPost.getQuestionPostId(),
        questionPost.getTitle() + " " +
        questionPost.getSubject() + " " +
        questionPost.getContent() + " " +
        (customTags != null ? String.join(" ", customTags) : ""),
        ContentType.QUESTION
    );

    // 질문 글 등록 시 엽전 100냥 감소
    yeopjeonService.processYeopjeon(member, YeopjeonAction.CREATE_QUESTION_POST);

    // 질문 글 등록 시 설정한 엽전 현상금 감소
    yeopjeonService.processYeopjeon(member, YeopjeonAction.REWARD_YEOPJEON, command.getRewardYeopjeon());

    // 질문 글 등록 시 경험치 증가
    expService.processExp(member, ExpAction.CREATE_QUESTION_POST);

    return QuestionDto.builder()
        .questionPost(savedQuestionPost)
        .mediaFiles(savedMediaFiles)
        .customTags(customTags)
        .build();
  }

  /**
   * <h3>특정 질문 글 조회</h3>
   * <ol>
   *   <li>조회수 증가</li>
   *   <li>좋아요 여부 확인</li>
   *   <li>답변 목록 조회</li>
   *   <li>커스텀 태그 조회</li>
   * </ol>
   *
   * @param command postId
   * @return 질문글, 답변목록, 커스텀태그 정보
   */
  @Transactional
  public QuestionDto getQuestionPost(QuestionCommand command) {
    // 질문 게시글 조회
    QuestionPost questionPost = questionPostRepository.findById(command.getPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 조회수 증가 (Redis 락을 사용하여 보호)
    String lockKey = "lock:questionPost:" + command.getPostId();
    redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {
      questionPost.increaseViewCount();
      log.info("제목: {}, 조회수: {}", questionPost.getTitle(), questionPost.getViewCount());

      // 변경사항 저장
      questionPostRepository.save(questionPost);

      return null;
    });

    // 좋아요 누른 회원인지 확인
    Boolean isLiked = questionBoardLikeRepository
        .existsByQuestionBoardIdAndMemberId(command.getPostId(), command.getMemberId());

    questionPost.setIsLiked(isLiked);

    // 작성자 여부 확인
    Boolean isAuthor = questionPost.getMember().getMemberId().equals(command.getMemberId());
    questionPost.setIsAuthor(isAuthor);

    // 답변 조회 (없으면 null 반환)
    List<AnswerPost> answerPosts = answerPostRepository
        .findAllByQuestionPost(questionPost).orElse(null);

    // 커스텀 태그 조회 (없으면 null 반환)
    List<String> customTags = null;
    if (questionPostCustomTagRepository.existsByQuestionPostId(command.getPostId())) {
      customTags = questionPostCustomTagRepository
          .findAllByQuestionPostId(command.getPostId())
          .stream().map(QuestionPostCustomTag::getCustomTag)
          .collect(Collectors.toList());
    }
    questionPost.setCustomTags(customTags);

    // 첨부파일 mediaFiles 가져오기
    List<MediaFile> mediaFiles = mediaFileService.getMediaFilesByPostId(questionPost.getQuestionPostId());

    return QuestionDto.builder()
        .questionPost(questionPost)
        .answerPosts(answerPosts)
        .customTags(customTags)
        .mediaFiles(mediaFiles)
        .build();
  }

  /**
   * 미답변 질문글 조회 + 단과대 필터링 - 최신순 정렬 (createdDate DESC)
   *
   * @return 질문글 페이지 정보
   */
  @Transactional(readOnly = true)
  public QuestionDto findAllQuestionPostsNotAnswered(QuestionCommand command) {

    // 단과대가 비어있는 경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getFaculty() != null && command.getFaculty().isEmpty()) {
      command.setFaculty(null);
    }

    Pageable pageable = PageRequest.of(command.getPageNumber(),
        command.getPageSize(),
        Sort.by("createdDate").descending());

    Page<QuestionPost> questionPostPage = questionPostRepository
        .findNotAnsweredQuestionByFilter(command.getFaculty(), pageable);
    questionPostPage.stream().forEach(questionPostCustomTagService::findQuestionPostCustomTags);

    return QuestionDto.builder()
        .questionPostsPage(questionPostPage)
        .build();
  }

  /**
   * 질문글 필터링
   * [필터링]
   * 1. 교과목명
   * 2. 정적태그 (최대 2개)
   * 3. 단과대
   * 4. 채택상태 (전체/채택/미채택)
   * <p>
   * [정렬]
   * 1. 최신순 (default)
   * 2. 좋아요순
   * 3. 조회순
   * 4. 엽전현상금순
   * 5. 엽전현상금 최신순
   * <p>
   * 엽전 현상금 순 조회 시 자동으로 미채택된 글만 조회합니다.
   *
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

    // 단과대가 비어있는 경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getFaculty() != null && command.getFaculty().isEmpty()) {
      command.setFaculty(null);
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
    SortType sortType = (command.getSortType() != null) ? command.getSortType() : LATEST;
    if (!sortType.equals(LATEST) &&
        !sortType.equals(MOST_LIKED) &&
        !sortType.equals(REWARD_YEOPJEON_DESCENDING) &&
        !sortType.equals(REWARD_YEOPJEON_LATEST) &&
        !sortType.equals(VIEW_COUNT)) {
      log.error("잘못된 sortType 요청입니다. 요청된 sortType: {}", command.getSortType());
      throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
    }

    // 엽전 현상금 관련 필터링 시
    boolean isRewardYeopjeonRequest = false;
    if (sortType.equals(REWARD_YEOPJEON_DESCENDING) || sortType.equals(REWARD_YEOPJEON_LATEST)) {
      log.debug("엽전 현상금 정렬 & 엽전 현상금이 존재하는 최신순 정렬 시에는 엽전 현상금이 존재하는 글만 반환됩니다");
      isRewardYeopjeonRequest = true;
    }

    // 엽전현상금순 및 엽전현상금 최신순으로 정렬 시 미채택 글만 조회
    if (sortType.equals(REWARD_YEOPJEON_DESCENDING) && !chaetaekStatus.equals(ChaetaekStatus.NO_CHAETAEK)) {
      log.warn("엽전 현상금 순으로 정렬 시 미채택된 글만 조회 가능합니다. 요청 chaetaekStatue: {}", chaetaekStatus);
      chaetaekStatus = ChaetaekStatus.NO_CHAETAEK;
    }

    // 엽전 현상금이 존재하는 최신 순 정렬 시
    if (sortType.equals(REWARD_YEOPJEON_LATEST)) {
      sortType = LATEST;
    }

    Sort sort = getJpqlSortOrder(sortType);

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort);

    // chaetaekStatus를 String으로 변환하여 전달
    Page<QuestionPost> questionPostPage = questionPostRepository.findQuestionPostsByFilter(
        command.getSubject(),
        command.getFaculty(),
        command.getQuestionPresetTags(),
        chaetaekStatus.name(), // Enum을 String으로 변환하여 전달
        isRewardYeopjeonRequest,
        pageable
    );
    questionPostPage.stream().forEach(questionPostCustomTagService::findQuestionPostCustomTags);

    return QuestionDto.builder()
        .questionPostsPage(questionPostPage)
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
