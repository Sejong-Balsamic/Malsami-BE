package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentPost;
import com.balsamic.sejongmalsami.object.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularPostService {

  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;

  public static final String DAILY_QUESTION_POSTS_KEY = "'dailyPopularQuestionPosts'";
  public static final String WEEKLY_QUESTION_POSTS_KEY = "'weeklyPopularQuestionPosts'";
  public static final String DAILY_DOCUMENT_POSTS_KEY = "'dailyPopularDocumentPosts'";
  public static final String WEEKLY_DOCUMENT_POSTS_KEY = "'weeklyPopularDocumentPosts'";

  // 30분마다 일간 인기글 점수 계산
  @Scheduled(fixedRate = 30 * 60 * 1000) // 30분마다 실행
  public void calculateDailyScore() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    // 질문글
    List<QuestionPost> questionPosts = questionPostRepository.findQuestionPostsAfter(yesterday);

    for (QuestionPost questionPost : questionPosts) {
      questionPost.updateDailyScore(calculateQuestionPostScore(questionPost));
      questionPostRepository.save(questionPost);
    }

    // 자료글
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(yesterday);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(calculateDocumentPostScore(documentPost));
      documentPostRepository.save(documentPost);
    }

    // 캐시 갱신
    updatePopularQuestionPostsCache();
    updatePopularDocumentPostsCache();
  }

  // 6시간마다 주간 인기글 점수 계산
  @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6시간마다 실행
  public void calculateWeeklyScore() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    // 질문글
    List<QuestionPost> posts = questionPostRepository.findQuestionPostsAfter(lastWeek);

    for (QuestionPost post : posts) {
      post.updateWeeklyScore(calculateQuestionPostScore(post));
      questionPostRepository.save(post);
    }

    // 자료글
    List<DocumentPost> documentPosts = documentPostRepository.findDocumentPostsAfter(lastWeek);

    for (DocumentPost documentPost : documentPosts) {
      documentPost.updateDailyScore(calculateDocumentPostScore(documentPost));
      documentPostRepository.save(documentPost);
    }

    // 캐시 갱신
    updatePopularQuestionPostsCache();
    updatePopularDocumentPostsCache();
  }

  // 점수 계산 (답변수 * 3 + 좋아요수 * 2 + 조회수)
  private Integer calculateQuestionPostScore(QuestionPost post) {
    return post.getAnswerCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

  // 점수 계산 (다운수 * 3 + 좋아요수 * 2 + 조회수)
  private Integer calculateDocumentPostScore(DocumentPost post) {
    return post.getDownloadCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

  // 캐시된 일간 질문 인기글 가져오기
  @Cacheable(value = "popularQuestionPosts", key = DAILY_QUESTION_POSTS_KEY)
  public List<QuestionPost> getDailyPopularQuestionPosts() {
    return questionPostRepository.findTop30ByOrderByDailyScoreDesc();
  }

  // 캐시된 주간 질문 인기글 가져오기
  @Cacheable(value = "popularQuestionPosts", key = WEEKLY_QUESTION_POSTS_KEY)
  public List<QuestionPost> getWeeklyPopularQuestionPosts() {
    return questionPostRepository.findTop30ByOrderByWeeklyScoreDesc();
  }

  // 캐시된 일간 자료 인기글 가져오기
  @Cacheable(value = "popularDocumentPosts", key = DAILY_DOCUMENT_POSTS_KEY)
  public List<DocumentPost> getDailyPopularDocumentPosts() {
    return documentPostRepository.findTop30ByOrderByDailyScoreDesc();
  }

  // 캐시된 주간 자료 인기글 가져오기
  @Cacheable(value = "popularDocumentPosts", key = WEEKLY_DOCUMENT_POSTS_KEY)
  public List<DocumentPost> getWeeklyPopularDocumentPosts() {
    return documentPostRepository.findTop30ByOrderByWeeklyScoreDesc();
  }

  // 캐시 갱신
  @CacheEvict(value = "popularQuestionPosts", allEntries = true)
  public void updatePopularQuestionPostsCache() {
    log.info("질문 인기글 캐시 갱신");
  }
  @CacheEvict(value = "popularDocumentPosts", allEntries = true)
  public void updatePopularDocumentPostsCache() {
    log.info("자료 인기글 캐시 갱신");
  }
}
