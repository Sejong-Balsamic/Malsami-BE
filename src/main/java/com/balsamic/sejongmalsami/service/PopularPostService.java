package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentDto;
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
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    // 질문글
    List<QuestionPost> questionPosts = questionPostRepository.findQuestionPostsAfter(yesterday);

    for (QuestionPost post : questionPosts) {
      post.updateDailyScore(scoreCalculator.calculateQuestionPostDailyScore(post));
      questionPostRepository.save(post);
    }

    // 자료글
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(yesterday);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(scoreCalculator.calculateDocumentPostDailyScore(documentPost));
      documentPostRepository.save(documentPost);
    }

    // 캐시 갱신
    updatePopularQuestionPostsCache();
    updatePopularDocumentPostsCache();
  }

  // 6시간마다 주간 인기글 점수 계산
  @Async
  @Transactional
  @Scheduled(fixedRate = WEEKLY_SCHEDULED_RATE) // 6시간마다 실행
  public void calculateWeeklyScore() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    // 질문글
    List<QuestionPost> posts = questionPostRepository.findQuestionPostsAfter(lastWeek);

    for (QuestionPost post : posts) {
      post.updateWeeklyScore(scoreCalculator.calculateQuestionPostWeeklyScore(post));
      questionPostRepository.save(post);
    }

    // 자료글
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(lastWeek);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(scoreCalculator.calculateDocumentPostWeeklyScore(documentPost));
      documentPostRepository.save(documentPost);
    }

    // 캐시 갱신
    updatePopularQuestionPostsCache();
    updatePopularDocumentPostsCache();
  }

  // 캐시된 일간 질문 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = QUESTION_POST_CACHE_VALUE, key = DAILY_QUESTION_POSTS_KEY)
  public QuestionDto getDailyPopularQuestionPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    return QuestionDto.builder()
        .questionPosts(questionPostRepository
            .findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(yesterday))
        .build();
  }

  // 캐시된 주간 질문 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = QUESTION_POST_CACHE_VALUE, key = WEEKLY_QUESTION_POSTS_KEY)
  public QuestionDto getWeeklyPopularQuestionPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    return QuestionDto.builder()
        .questionPosts(questionPostRepository
            .findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek))
        .build();
  }

  // 캐시된 일간 자료 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = DOCUMENT_POST_CACHE_VALUE, key = DAILY_DOCUMENT_POSTS_KEY)
  public DocumentDto getDailyPopularDocumentPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    return DocumentDto.builder()
        .documentPosts(documentPostRepository
            .findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(yesterday))
        .build();
  }

  // 캐시된 주간 자료 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = DOCUMENT_POST_CACHE_VALUE, key = WEEKLY_DOCUMENT_POSTS_KEY)
  public DocumentDto getWeeklyPopularDocumentPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    return DocumentDto.builder()
        .documentPosts(documentPostRepository
            .findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek))
        .build();
  }

  // 캐시 갱신
  @Async
  @Transactional
  @CacheEvict(value = QUESTION_POST_CACHE_VALUE, allEntries = true)
  public void updatePopularQuestionPostsCache() {
    log.info("질문 인기글 캐시 갱신");
  }

  @Async
  @Transactional
  @CacheEvict(value = DOCUMENT_POST_CACHE_VALUE, allEntries = true)
  public void updatePopularDocumentPostsCache() {
    log.info("자료 인기글 캐시 갱신");
  }
}
