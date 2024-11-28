package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  private static final long QUESTION_DAILY_SCHEDULED_RATE = 30 * 60 * 1000L; // 30분
  private static final long QUESTION_WEEKLY_SCHEDULED_RATE = 6 * 60 * 60 * 1000L; // 6시간

  // 30분마다 일간 인기 질문글 점수 계산
  @Async
  @Transactional
  @Scheduled(fixedRate = QUESTION_DAILY_SCHEDULED_RATE) // 30분마다 실행
  public void calculateQuestionDailyScore() {

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
  }

  // 6시간마다 주간 인기 질문글 점수 계산
  @Async
  @Transactional
  @Scheduled(fixedRate = QUESTION_WEEKLY_SCHEDULED_RATE) // 6시간마다 실행
  public void calculateQuestionWeeklyScore() {

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
  }

  /**
   * <h3>매일 자정에 일간 인기 자료글 업데이트</h3>
   * <p>자료 글 일간 인기점수 초기화</p>
   * <p>인기글 계산 로직에 따라 일간 인기점수 계산</p>
   * <p>최소 기준을 만족한 자료 글들에 대해서 상위 최대 50개 글 저장</p>
   * <p>인기글에 등록 된 글 isPopular = true 변경</p>
   */
  @Async
  @Transactional
  @Scheduled(cron = "0 0 0 * * *")
  public void calculateDocumentDailyScore() {

    // 자료 글 일간 인기점수 초기화
    documentPostRepository.resetDailyScore();

    // 일간 인기점수 계산
    List<DocumentPost> posts = documentPostRepository.findAll();
    for (DocumentPost post : posts) {
      post.updateDailyScore(scoreCalculator.calculateDocumentPostDailyScore(post));
      documentPostRepository.save(post);
    }
  }

  /**
   * <h3>매주 월요일 자정에 주간 인기 자료글 업데이트</h3>
   * <p>자료 글 일간 인기점수 초기화</p>
   * <p>인기글 계산 로직에 따라 주간 인기점수 계산</p>
   * <p>최소 기준을 만족한 자료 글들에 대해서 상위 최대 50개 글 저장</p>
   * <p>인기글에 등록 된 글 isPopular = true 변경</p>
   */
  @Async
  @Transactional
  @Scheduled(cron = "0 0 0 * * Mon")
  public void calculateDocumentWeeklyScore() {

    // 자료 글 주간 인기점수 초기화
    documentPostRepository.resetWeeklyScore();

    // 주간 인기점수 계산
    List<DocumentPost> posts = documentPostRepository.findAll();
    for (DocumentPost post : posts) {
      post.updateWeeklyScore(scoreCalculator.calculateDocumentPostWeeklyScore(post));
      documentPostRepository.save(post);
    }
  }

  /**
   * <h3>일간 인기 질문 글 조회 로직
   * <p>24시간 이내에 작성 된 질문 글을 대상으로 조회합니다.</p>
   * <p>일간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getDailyPopularQuestionPosts(QuestionCommand command) {

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("dailyScore").descending()
    );

    Page<QuestionPost> posts = questionPostRepository
        .findAllByCreatedDateAfter(yesterday, pageable);

    return QuestionDto.builder()
        .questionPostsPage(posts)
        .build();
  }

  /**
   * <h3>주간 인기 질문글 조회 로직
   * <p>7일 이내에 작성 된 질문 글을 대상으로 조회합니다.</p>
   * <p>주간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getWeeklyPopularQuestionPosts(QuestionCommand command) {

    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("weeklyScore").descending()
    );

    Page<QuestionPost> posts = questionPostRepository
        .findAllByCreatedDateAfter(lastWeek, pageable);

    return QuestionDto.builder()
        .questionPostsPage(posts)
        .build();
  }

  /**
   * <h3>일간 인기 자료글 조회 로직</h3>
   * <p>일간 자료 글 인기점수는 24시간마다 초기화됩니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public DocumentDto getDailyPopularDocumentPosts(DocumentCommand command) {

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("dailyScore").descending()
    );

    Page<DocumentPost> posts = documentPostRepository
        .findAllByCreatedDateAfter(yesterday, pageable);

    return DocumentDto.builder()
        .documentPostsPage(posts)
        .build();
  }

  /**
   * <h3>주간 인기 자료글 조회 로직</h3>
   * <p>주간 자료 글 인기점수는 7일마다 초기화됩니다.
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional
  public DocumentDto getWeeklyPopularDocumentPosts(DocumentCommand command) {

    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("weeklyScore").descending()
    );

    Page<DocumentPost> posts = documentPostRepository
        .findAllByCreatedDateAfter(lastWeek, pageable);

    return DocumentDto.builder()
        .documentPostsPage(posts)
        .build();
  }


}
