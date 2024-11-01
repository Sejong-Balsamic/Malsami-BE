package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.ScoreCalculator;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
  // 클래스 내부에서 @Cacheable 메서드를 호출하기 위해 ApplicationContext 사용
  private final ApplicationContext applicationContext;

  private static final long DAILY_SCHEDULED_RATE = 30 * 60 * 1000L;
  private static final long WEEKLY_SCHEDULED_RATE = 6 * 60 * 60 * 1000L;
  private static final String QUESTION_POST_CACHE_VALUE = "popularQuestionPosts";
  private static final String DOCUMENT_POST_CACHE_VALUE = "popularDocumentPosts";
  private static final String DAILY_QUESTION_POSTS_KEY = "'dailyPopularQuestionPosts'";
  private static final String WEEKLY_QUESTION_POSTS_KEY = "'weeklyPopularQuestionPosts'";
  private static final String DAILY_DOCUMENT_POSTS_KEY = "'dailyPopularDocumentPosts'";
  private static final String WEEKLY_DOCUMENT_POSTS_KEY = "'weeklyPopularDocumentPosts'";

  // 30분마다 일간 인기글 점수 계산
  @Async
  @Transactional
  @Scheduled(fixedRate = DAILY_SCHEDULED_RATE) // 30분마다 실행
  public void calculateDailyScore() {

    // 일간 인기글 질문/자료 캐시 삭제
    deleteDailyPopularQuestionPostsCache();
    deleteDailyPopularDocumentPostsCache();

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    Integer postCounts = questionPostRepository.countByCreatedDateAfter(yesterday);

    if (postCounts <= 0) {
      postCounts = 1;
    }
    // 24시간 이내에 작성된 모든 질문글 일간 점수 업데이트
    Pageable pageable = PageRequest.of(0, postCounts);
    Page<QuestionPost> posts = questionPostRepository.findByCreatedDateAfter(yesterday, pageable);

    for (QuestionPost post : posts) {
      post.updateDailyScore(scoreCalculator.calculateQuestionPostDailyScore(post));
      questionPostRepository.save(post);
    }

    // 자료글 TODO: 인기 자료글 로직 수정
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(yesterday);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(scoreCalculator.calculateDocumentPostDailyScore(documentPost));
      documentPostRepository.save(documentPost);
    }
  }

  // 6시간마다 주간 인기글 점수 계산
  @Async
  @Transactional
  @Scheduled(fixedRate = WEEKLY_SCHEDULED_RATE) // 6시간마다 실행
  public void calculateWeeklyScore() {

    // 주간 인기글 질문/자료 캐시 삭제
    deleteWeeklyPopularQuestionPostsCache();
    deleteWeeklyPopularDocumentPostsCache();

    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    Integer postCounts = questionPostRepository.countByCreatedDateAfter(lastWeek);

    if (postCounts <= 0) {
      postCounts = 1;
    }
    // 7일 이내에 작성된 모든 질문글 주간 점수 업데이트
    Pageable pageable = PageRequest.of(0, postCounts);
    Page<QuestionPost> posts = questionPostRepository.findByCreatedDateAfter(lastWeek, pageable);

    for (QuestionPost post : posts) {
      post.updateWeeklyScore(scoreCalculator.calculateQuestionPostWeeklyScore(post));
      questionPostRepository.save(post);
    }

    // 자료글 TODO: 인기 자료글 로직 수정
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(lastWeek);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(scoreCalculator.calculateDocumentPostWeeklyScore(documentPost));
      documentPostRepository.save(documentPost);
    }
  }

  /**
   * 캐시된 일간 인기 질문글 조회 로직
   *
   * @param command <br>
   * Integer pageSize : 조회하고 싶은 일간 인기 질문글 개수 (default = 30)
   *
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getDailyPopularQuestionPosts(QuestionCommand command) {

    if (command.getPageSize() == null) {
      command.setPageSize(30);
    }

    // 캐시에서 일간 인기 질문글 조회
    List<QuestionPost> cachedPosts = applicationContext
        .getBean(PopularPostService.class)
        .updateDailyPopularQuestionPostsCache()
        .subList(0, command.getPageSize());

    // pageSize개수 만큼 List를 Page로 변환
    Pageable pageable = PageRequest.of(0, command.getPageSize());
    Page<QuestionPost> posts = new PageImpl<>(cachedPosts, pageable, cachedPosts.size());

    return QuestionDto.builder()
        .questionPosts(posts)
        .build();
  }

  /**
   * 캐시된 주간 인기 질문글 조회 로직
   *
   * @param command <br>
   * Integer pageSize : 조회하고 싶은 주간 인기 질문글 개수 (default = 30)
   *
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getWeeklyPopularQuestionPosts(QuestionCommand command) {

    if (command.getPageSize() == null) {
      command.setPageSize(30);
    }

    // 캐시에서 일간 인기 질문글 pageSize 개수만큼 조회
    List<QuestionPost> cachedPosts = applicationContext
        .getBean(PopularPostService.class)
        .updateWeeklyPopularQuestionPostsCache()
        .subList(0, command.getPageSize());

    log.info("캐시된 데이터 파싱 {}", cachedPosts);

    // pageSize개수 만큼 List를 Page로 변환
    Pageable pageable = PageRequest.of(0, command.getPageSize());
    Page<QuestionPost> posts = new PageImpl<>(cachedPosts, pageable, cachedPosts.size());

    return QuestionDto.builder()
        .questionPosts(posts)
        .build();
  }

  // 캐시된 일간 자료 인기글 가져오기 TODO: 인기 자료글 로직 수정
  @Transactional(readOnly = true)
  @Cacheable(value = DOCUMENT_POST_CACHE_VALUE, key = DAILY_DOCUMENT_POSTS_KEY)
  public DocumentDto getDailyPopularDocumentPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    return DocumentDto.builder()
        .documentPosts(documentPostRepository
            .findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(yesterday))
        .build();
  }

  // 캐시된 주간 자료 인기글 가져오기 TODO: 인기 자료글 로직 수정
  @Transactional(readOnly = true)
  @Cacheable(value = DOCUMENT_POST_CACHE_VALUE, key = WEEKLY_DOCUMENT_POSTS_KEY)
  public DocumentDto getWeeklyPopularDocumentPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    return DocumentDto.builder()
        .documentPosts(documentPostRepository
            .findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek))
        .build();
  }

  // 일간 인기 질문 글 캐시 업데이트 (상위 50개)
  @Transactional
  @Cacheable(value = QUESTION_POST_CACHE_VALUE, key = DAILY_QUESTION_POSTS_KEY)
  public List<QuestionPost> updateDailyPopularQuestionPostsCache() {

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    return questionPostRepository
        .findTop50ByCreatedDateAfterOrderByDailyScoreDesc(yesterday);
  }

  // 주간 인기 질문 글 캐시 업데이트 (상위 50개)
  @Transactional
  @Cacheable(value = QUESTION_POST_CACHE_VALUE, key = WEEKLY_QUESTION_POSTS_KEY)
  public List<QuestionPost> updateWeeklyPopularQuestionPostsCache() {

    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    return questionPostRepository
        .findTop50ByCreatedDateAfterOrderByWeeklyScoreDesc(lastWeek);
  }

  // 일간 인기 질문 글 캐시 삭제
  @Transactional
  @CacheEvict(value = QUESTION_POST_CACHE_VALUE, key = DAILY_QUESTION_POSTS_KEY)
  public void deleteDailyPopularQuestionPostsCache() {
    log.info("일간 인기 질문글 캐시 삭제");
  }

  // 주간 인기 질문 글 캐시 삭제
  @Transactional
  @CacheEvict(value = QUESTION_POST_CACHE_VALUE, key = WEEKLY_QUESTION_POSTS_KEY)
  public void deleteWeeklyPopularQuestionPostsCache() {
    log.info("주간 인기 질문글 캐시 삭제");
  }

  // 일간 인기 자료 글 캐시 삭제
  @Transactional
  @CacheEvict(value = DOCUMENT_POST_CACHE_VALUE, key = DAILY_DOCUMENT_POSTS_KEY)
  public void deleteDailyPopularDocumentPostsCache() {
    log.info("일간 인기 자료글 캐시 삭제");
  }

  // 주간 인기 자료 글 캐시 삭제
  @Transactional
  @CacheEvict(value = DOCUMENT_POST_CACHE_VALUE, key = WEEKLY_DOCUMENT_POSTS_KEY)
  public void deleteWeeklyPopularDocumentPostsCache() {
    log.info("주간 인기 자료글 캐시 삭제");
  }
}
