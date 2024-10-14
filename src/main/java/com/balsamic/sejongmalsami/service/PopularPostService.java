package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.DocumentPost;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.MethodUtil;
import com.balsamic.sejongmalsami.util.config.ScoreConfig;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularPostService {

  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final ScoreConfig scoreConfig;

  private static final long DAILY_SCHEDULED_RATE = 30 * 60 * 1000L;
  private static final long WEEKLY_SCHEDULED_RATE = 6 * 60 * 60 * 1000L;
  private static final String QUESTION_POST_CACHE_VALUE = "popularQuestionPosts";
  private static final String DOCUMENT_POST_CACHE_VALUE = "popularDocumentPosts";
  private static final String DAILY_QUESTION_POSTS_KEY = "'dailyPopularQuestionPosts'";
  private static final String WEEKLY_QUESTION_POSTS_KEY = "'weeklyPopularQuestionPosts'";
  private static final String DAILY_DOCUMENT_POSTS_KEY = "'dailyPopularDocumentPosts'";
  private static final String WEEKLY_DOCUMENT_POSTS_KEY = "'weeklyPopularDocumentPosts'";

  // 30분마다 일간 인기글 점수 계산
  @Transactional
  @Scheduled(fixedRate = DAILY_SCHEDULED_RATE) // 30분마다 실행
  public void calculateDailyScore() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    // 질문글
    List<QuestionPost> questionPosts = questionPostRepository.findQuestionPostsAfter(yesterday);

    for (QuestionPost questionPost : questionPosts) {
      questionPost.updateDailyScore(scoreConfig.calculateQuestionPostDailyScore(questionPost));
      questionPostRepository.save(questionPost);
    }

    // 자료글
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(yesterday);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(scoreConfig.calculateDocumentPostDailyScore(documentPost));
      documentPostRepository.save(documentPost);
    }

    // 캐시 갱신
    updatePopularQuestionPostsCache();
    updatePopularDocumentPostsCache();
  }

  // 6시간마다 주간 인기글 점수 계산
  @Transactional
  @Scheduled(fixedRate = WEEKLY_SCHEDULED_RATE) // 6시간마다 실행
  public void calculateWeeklyScore() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    // 질문글
    List<QuestionPost> posts = questionPostRepository.findQuestionPostsAfter(lastWeek);

    for (QuestionPost post : posts) {
      post.updateWeeklyScore(scoreConfig.calculateQuestionPostWeeklyScore(post));
      questionPostRepository.save(post);
    }

    // 자료글
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(lastWeek);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(scoreConfig.calculateDocumentPostWeeklyScore(documentPost));
      documentPostRepository.save(documentPost);
    }

    // 캐시 갱신
    updatePopularQuestionPostsCache();
    updatePopularDocumentPostsCache();
  }

  // 캐시된 일간 질문 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = QUESTION_POST_CACHE_VALUE, key = DAILY_QUESTION_POSTS_KEY)
  public List<QuestionDto> getDailyPopularQuestionPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<QuestionPost> questionPostList = questionPostRepository
        .findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(yesterday);
    return MethodUtil.convertToDtoList(questionPostList, questionPost ->
        QuestionDto.builder()
            .questionPost(questionPost)
            .build());
  }

  // 캐시된 주간 질문 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = QUESTION_POST_CACHE_VALUE, key = WEEKLY_QUESTION_POSTS_KEY)
  public List<QuestionDto> getWeeklyPopularQuestionPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    List<QuestionPost> questionPostList = questionPostRepository
        .findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek);
    return MethodUtil.convertToDtoList(questionPostList, questionPost ->
        QuestionDto.builder()
            .questionPost(questionPost)
            .build());
  }

  // 캐시된 일간 자료 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = DOCUMENT_POST_CACHE_VALUE, key = DAILY_DOCUMENT_POSTS_KEY)
  public List<DocumentDto> getDailyPopularDocumentPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<DocumentPost> documentPostList = documentPostRepository.
        findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(yesterday);
    return MethodUtil.convertToDtoList(documentPostList, documentPost ->
        DocumentDto.builder()
            .documentPost(documentPost)
            .build());
  }

  // 캐시된 주간 자료 인기글 가져오기
  @Transactional(readOnly = true)
  @Cacheable(value = DOCUMENT_POST_CACHE_VALUE, key = WEEKLY_DOCUMENT_POSTS_KEY)
  public List<DocumentDto> getWeeklyPopularDocumentPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    List<DocumentPost> documentPostList = documentPostRepository.
        findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek);
    return MethodUtil.convertToDtoList(documentPostList, documentPost ->
        DocumentDto.builder()
            .documentPost(documentPost)
            .build());
  }

  // 캐시 갱신
  @Transactional
  @CacheEvict(value = QUESTION_POST_CACHE_VALUE, allEntries = true)
  public void updatePopularQuestionPostsCache() {
    log.info("질문 인기글 캐시 갱신");
  }

  @Transactional
  @CacheEvict(value = DOCUMENT_POST_CACHE_VALUE, allEntries = true)
  public void updatePopularDocumentPostsCache() {
    log.info("자료 인기글 캐시 갱신");
  }
}
