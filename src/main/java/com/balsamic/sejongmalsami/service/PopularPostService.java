package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.ContentType.DOCUMENT;
import static com.balsamic.sejongmalsami.object.constants.ContentType.QUESTION;

import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.ScoreCalculator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularPostService {

  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final ScoreCalculator scoreCalculator;
  private final RedisTemplate<String, Object> redisTemplate;
  private static final long QUESTION_DAILY_SCHEDULED_RATE = 60 * 60 * 1000L; // 1시간
  private static final long QUESTION_WEEKLY_SCHEDULED_RATE = 6 * 60 * 60 * 1000L; // 6시간
  private static final long DOCUMENT_DAILY_SCHEDULED_RATE = 24 * 60 * 60 * 1000L; // 24시간
  private static final long DOCUMENT_WEEKLY_SCHEDULED_RATE = 7 * 24 * 60 * 60 * 1000L; // 7일
  private static final int SAVE_POPULAR_POST_COUNT = 30; // 저장할 인기글 개수
  private static final String QUESTION_DAILY_KEY = "question:daily";
  private static final String QUESTION_WEEKLY_KEY = "question:weekly";
  private static final String DOCUMENT_DAILY_KEY = "document:daily";
  private static final String DOCUMENT_WEEKLY_KEY = "document:weekly";

  /**
   * <h3>일간 인기 질문 글 조회 로직
   * <p>Redis에서 QUESTION_DAILY_KEY로 저장된 ID 목록을 조회합니다.</p>
   * <p>24시간 이내에 작성 된 질문 글을 대상으로 조회합니다.</p>
   * <p>일간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getDailyPopularQuestionPosts() {
    return (QuestionDto) getPagedPosts(QUESTION_DAILY_KEY, QUESTION, true);
  }

  /**
   * <h3>주간 인기 질문글 조회 로직
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>7일 이내에 작성 된 질문 글을 대상으로 조회합니다.</p>
   * <p>주간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getWeeklyPopularQuestionPosts() {
    return (QuestionDto) getPagedPosts(QUESTION_WEEKLY_KEY, QUESTION, false);
  }

  /**
   * <h3>일간 인기 자료글 조회 로직</h3>
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>일간 자료 글 인기점수는 24시간마다 초기화됩니다.</p>
   * <p>일간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @return
   */
  @Transactional(readOnly = true)
  public DocumentDto getDailyPopularDocumentPosts() {
    return (DocumentDto) getPagedPosts(DOCUMENT_DAILY_KEY, DOCUMENT, true);
  }

  /**
   * <h3>주간 인기 자료글 조회 로직</h3>
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>주간 자료 글 인기점수는 7일마다 초기화됩니다.
   * <p>주간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @return
   */
  @Transactional
  public DocumentDto getWeeklyPopularDocumentPosts() {
    return (DocumentDto) getPagedPosts(DOCUMENT_WEEKLY_KEY, DOCUMENT, false);
  }

  /**
   * <h3>일간 인기 질문 글 점수 업데이트</h3>
   * <p>1시간 마다 일간 인기 질문글 점수 계산 후 상위 30개 캐시 저장</p>
   * <p>현재 시간으로부터 24시간 이내에 작성된 글만 점수 계산</p>
   */
  @Async
  @Transactional
  @Scheduled(fixedRate = QUESTION_DAILY_SCHEDULED_RATE)
  public void calculateQuestionDailyScore() {

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    // 24시간 이내에 작성된 모든 질문글 일간 점수 업데이트
    List<QuestionPost> posts = questionPostRepository
        .findAllByCreatedDateAfter(yesterday)
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 일간 점수 업데이트
    for (QuestionPost post : posts) {
      post.setDailyScore(scoreCalculator.calculateQuestionPostDailyScore(post));
    }
    questionPostRepository.saveAll(posts);

    // 상위 30개 글 추출
    List<QuestionPost> topPosts = posts.subList(0, Math.min(SAVE_POPULAR_POST_COUNT, posts.size()));

    // 캐시에 Id 값을 추출 후 String 형태로 변환하여 저장
    List<String> topPostIds = topPosts.stream()
        .map(post -> post.getQuestionPostId().toString())
        .toList();

    // Redis에 저장
    redisTemplate.delete(QUESTION_DAILY_KEY);
    for (String id : topPostIds) {
      redisTemplate.opsForList().rightPush(QUESTION_DAILY_KEY, id);
    }
  }

  /**
   * <h3>주간 인기 질문 글 점수 업데이트</h3>
   * <p>6시간마다 주간 인기 질문글 점수 계산 후 상위 30개 데이터 저장</p>
   * <p>현재 시간으로부터 7일 이내에 작성된 글만 점수 계산</p>
   */
  @Async
  @Transactional
  @Scheduled(fixedRate = QUESTION_WEEKLY_SCHEDULED_RATE)
  public void calculateQuestionWeeklyScore() {

    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    // 7일 이내에 작성된 모든 질문글 주간 점수 업데이트
    List<QuestionPost> posts = questionPostRepository
        .findAllByCreatedDateAfter(lastWeek)
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    // 주간 점수 업데이트
    for (QuestionPost post : posts) {
      post.setWeeklyScore(scoreCalculator.calculateQuestionPostWeeklyScore(post));
    }
    questionPostRepository.saveAll(posts);

    // 상위 30개 글 추출
    List<QuestionPost> topPosts = posts.subList(0, Math.min(SAVE_POPULAR_POST_COUNT, posts.size()));

    // 캐시에 Id 값을 추출 후 저장
    List<String> topPostIds = topPosts.stream()
        .map(post -> post.getQuestionPostId().toString())
        .toList();

    // Redis에 저장
    redisTemplate.delete(QUESTION_WEEKLY_KEY);
    for (String id : topPostIds) {
      redisTemplate.opsForList().rightPush(QUESTION_WEEKLY_KEY, id);
    }
  }

  /**
   * <h3>매일 자정에 일간 인기 자료글 업데이트</h3>
   * <p>자료 글 일간 인기점수 초기화</p>
   * <p>인기글 계산 로직에 따라 일간 인기점수 계산</p>
   * <p>인기글에 등록 된 상위 30개 글 isPopular = true 변경</p>
   */
  @Async
  @Transactional
  @Scheduled(fixedRate = DOCUMENT_DAILY_SCHEDULED_RATE)
  // FIXME: 배포시 주석 처리 된 코드로 변경
//  @Scheduled(cron = "0 0 0 * * *")
  public void calculateDocumentDailyScore() {

    // 자료 글 일간 인기점수 초기화
    documentPostRepository.resetDailyScore();

    // 전체 자료글 dailyScore 계산
    List<DocumentPost> posts = documentPostRepository.findAll();
    for (DocumentPost curPost : posts) {
      long curDocumentPostDailyScore = scoreCalculator.calculateDocumentPostDailyScore(curPost);
      log.info("자료 게시글 : 일간 점수 업데이트 완료 : id={} score={}", curPost.getDocumentPostId(), curDocumentPostDailyScore);
      curPost.setDailyScore(curDocumentPostDailyScore);
    }
    documentPostRepository.saveAll(posts);

    // dailyScore 상위 30개 자료 글 조회
    Pageable pageable = PageRequest.of(
        0,
        SAVE_POPULAR_POST_COUNT,
        Sort.by("dailyScore").descending()
    );
    Page<DocumentPost> topPosts = documentPostRepository.findAll(pageable);

    // 상위 30개 글 isPopular = true 설정
    topPosts.forEach(post -> post.setIsPopular(true));
    documentPostRepository.saveAll(topPosts);

    List<String> topPostIds = topPosts.stream()
        .map(post -> post.getDocumentPostId().toString())
        .toList();

    // Redis에 저장
    redisTemplate.delete(DOCUMENT_DAILY_KEY);
    for (String id : topPostIds) {
      redisTemplate.opsForList().rightPush(DOCUMENT_DAILY_KEY, id);
    }
  }

  /**
   * <h3>매주 월요일 자정에 주간 인기 자료글 업데이트</h3>
   * <p>자료 글 일간 인기점수 초기화</p>
   * <p>인기글 계산 로직에 따라 주간 인기점수 계산</p>
   * <p>인기글에 등록 된 상위 10개 글 isPopular = true 변경</p>
   */
  @Async
  @Transactional
  @Scheduled(fixedRate = DOCUMENT_WEEKLY_SCHEDULED_RATE)
  // FIXME: 배포시 주석 처리 된 코드로 변경
//  @Scheduled(cron = "0 0 0 * * Mon")
  public void calculateDocumentWeeklyScore() {

    // 자료 글 주간 인기점수 초기화
    documentPostRepository.resetWeeklyScore();

    // 전체 자료글 weeklyScore 계산
    List<DocumentPost> posts = documentPostRepository.findAll();
    for (DocumentPost curPost : posts) {
      long curDocumentPostWeeklyScore = scoreCalculator.calculateDocumentPostWeeklyScore(curPost);
      log.info("자료 게시글 : 주간 점수 업데이트 완료 : id={} score={}", curPost.getDocumentPostId(), curDocumentPostWeeklyScore);
      curPost.setWeeklyScore(curDocumentPostWeeklyScore);
    }
    documentPostRepository.saveAll(posts);

    // weeklyScore 상위 30개 자료 글 조회
    Pageable pageable = PageRequest.of(
        0,
        SAVE_POPULAR_POST_COUNT,
        Sort.by("weeklyScore").descending()
    );
    Page<DocumentPost> topPosts = documentPostRepository.findAll(pageable);

    // 상위 30개 글 isPopular = true 설정 후 저장
    topPosts.forEach(post -> post.setIsPopular(true));
    documentPostRepository.saveAll(topPosts);

    List<String> topPostIds = topPosts.stream()
        .map(post -> post.getDocumentPostId().toString())
        .toList();

    // Redis에 저장
    redisTemplate.delete(DOCUMENT_WEEKLY_KEY);
    for (String id : topPostIds) {
      redisTemplate.opsForList().rightPush(DOCUMENT_WEEKLY_KEY, id);
    }
  }

  /**
   * Redis에서 key를 사용해 List로 저장된 PostId를 가져오는 메서드
   *
   * @param key
   * @return
   */
  private List<UUID> getIdsFromRedis(String key) {
    List<Object> rawIds = redisTemplate.opsForList().range(key, 0, -1);
    if (rawIds == null || rawIds.isEmpty()) {
      log.info("Redis에서 key: {}로 저장된 인기글 데이터가 없습니다.", key);
      return List.of();
    }

    return rawIds.stream()
        .map(obj -> (String) obj)
        .map(UUID::fromString)
        .collect(Collectors.toList());
  }

  /**
   * <h3>Redis에 저장된 PK 값을 해당 글로 반환합니다.</h3>
   *
   * @param key Redis 저장 키
   * @param contentType 질문 or 자료
   * @param daily true일 경우 일간 인기글
   * @return
   */
  private Object getPagedPosts(String key, ContentType contentType, boolean daily) {

    // Redis에 저장된 일간 인기 글 PK
    List<UUID> postIds = getIdsFromRedis(key);

    // PK 값에 해당하는 글 조회
    if (contentType.equals(QUESTION)) { // 질문 글
      List<QuestionPost> questionPosts = questionPostRepository.findAllById(postIds);
      if (daily) { // 질문글 dailyScore 내림차순
        questionPosts.sort(Comparator.comparing(QuestionPost::getDailyScore).reversed());
      } else { // 질문글 weeklyScore 내림차순
        questionPosts.sort(Comparator.comparing(QuestionPost::getWeeklyScore).reversed());
      }
      if (questionPosts.isEmpty()) {
        return null;
      }
      int postCount = Math.min(SAVE_POPULAR_POST_COUNT, questionPosts.size());
      Pageable pageable = PageRequest.of(0, postCount);
      Page<QuestionPost> questionPostPage = new PageImpl<>(
          questionPosts,
          pageable,
          postCount
      );
      return QuestionDto.builder()
          .questionPostsPage(questionPostPage)
          .build();
    } else if (contentType.equals(DOCUMENT)) { // 자료 글
      List<DocumentPost> documentPosts = documentPostRepository.findAllById(postIds);
      if (daily) { // 자료글 dailyScore 내림차순
        documentPosts.sort(Comparator.comparing(DocumentPost::getDailyScore).reversed());
      } else { // 자료글 weeklyScore 내림차순
        documentPosts.sort(Comparator.comparing(DocumentPost::getWeeklyScore).reversed());
      }
      int postCount = Math.min(SAVE_POPULAR_POST_COUNT, documentPosts.size());
      Pageable pageable = PageRequest.of(0, postCount);
      Page<DocumentPost> documentPostPage = new PageImpl<>(
          documentPosts,
          pageable,
          postCount
      );
      return DocumentDto.builder()
          .documentPostsPage(documentPostPage)
          .build();
    } else {
      log.error("잘못된 contentType 입니다. 요청 contentType: {}", contentType);
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }
  }
}
