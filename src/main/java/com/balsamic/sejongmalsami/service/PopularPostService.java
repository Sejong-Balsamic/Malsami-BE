package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.DocumentPostDto;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.QuestionPostDto;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

  public static final String DAILY_QUESTION_POSTS_KEY = "dailyPopularQuestionPosts";
  public static final String WEEKLY_QUESTION_POSTS_KEY = "weeklyPopularQuestionPosts";
  public static final String DAILY_DOCUMENT_POSTS_KEY = "dailyPopularDocumentPosts";
  public static final String WEEKLY_DOCUMENT_POSTS_KEY = "weeklyPopularDocumentPosts";

  // 30분마다 일간 인기글 점수 계산
  @Transactional
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
  @Transactional
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
  // TODO : 점수 계산 로직 작성
  private Integer calculateDocumentPostScore(DocumentPost post) {
    return post.getLikeCount() * 2 + post.getViewCount();
  }

  // 캐시된 일간 질문 인기글 가져오기
  @Cacheable(value = "popularQuestionPosts", key = DAILY_QUESTION_POSTS_KEY)
  public List<QuestionPostDto> getDailyPopularQuestionPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<QuestionPost> questionPostList = questionPostRepository
        .findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(yesterday);
    return convertToDtoList(questionPostList, questionPost ->
        QuestionPostDto.builder()
            .questionPost(questionPost)
            .build());
  }

  // 캐시된 주간 질문 인기글 가져오기
  @Cacheable(value = "popularQuestionPosts", key = WEEKLY_QUESTION_POSTS_KEY)
  public List<QuestionPostDto> getWeeklyPopularQuestionPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    List<QuestionPost> questionPostList = questionPostRepository
        .findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek);
    return convertToDtoList(questionPostList, questionPost ->
        QuestionPostDto.builder()
            .questionPost(questionPost)
            .build());
  }

  // 캐시된 일간 자료 인기글 가져오기
  @Cacheable(value = "popularDocumentPosts", key = DAILY_DOCUMENT_POSTS_KEY)
  public List<DocumentPostDto> getDailyPopularDocumentPosts() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<DocumentPost> documentPostList = documentPostRepository.
        findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(yesterday);
    return convertToDtoList(documentPostList, documentPost ->
        DocumentPostDto.builder()
            .documentPost(documentPost)
            .build());
  }

  // 캐시된 주간 자료 인기글 가져오기
  @Cacheable(value = "popularDocumentPosts", key = WEEKLY_DOCUMENT_POSTS_KEY)
  public List<DocumentPostDto> getWeeklyPopularDocumentPosts() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    List<DocumentPost> documentPostList = documentPostRepository.
        findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(lastWeek);
    return convertToDtoList(documentPostList, documentPost ->
        DocumentPostDto.builder()
            .documentPost(documentPost)
            .build());
  }

  // 캐시 갱신
  @Transactional
  @CacheEvict(value = "popularQuestionPosts", allEntries = true)
  public void updatePopularQuestionPostsCache() {
    log.info("질문 인기글 캐시 갱신");
  }

  @Transactional
  @CacheEvict(value = "popularDocumentPosts", allEntries = true)
  public void updatePopularDocumentPostsCache() {
    log.info("자료 인기글 캐시 갱신");
  }

  // 변환 메서드
  @NotNull
  private <T, D> List<D> convertToDtoList(List<T> entities, Function<T, D> converter) {
    return entities.stream()
        .map(converter)
        .collect(Collectors.toList());
  }
}
