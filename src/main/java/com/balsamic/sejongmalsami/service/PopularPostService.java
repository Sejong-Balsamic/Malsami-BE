package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.ContentType.DOCUMENT;
import static com.balsamic.sejongmalsami.object.constants.ContentType.QUESTION;
import static com.balsamic.sejongmalsami.object.constants.PopularType.DAILY;
import static com.balsamic.sejongmalsami.object.constants.PopularType.WEEKLY;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.PopularPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.PopularPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.ScoreCalculator;
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
  private final PopularPostRepository popularPostRepository;
  private final ScoreCalculator scoreCalculator;
  private static final long QUESTION_DAILY_SCHEDULED_RATE = 30 * 60 * 1000L; // 30분
  private static final long QUESTION_WEEKLY_SCHEDULED_RATE = 6 * 60 * 60 * 1000L; // 6시간
  private static final int SAVE_POPULAR_POST_COUNT = 30; // 저장할 인기글 개수

  // 30분마다 일간 인기 질문글 점수 계산 후 상위 30개 데이터 저장
  @Async
  @Transactional
  @Scheduled(fixedRate = QUESTION_DAILY_SCHEDULED_RATE) // 30분마다 실행
  public void calculateQuestionDailyScore() {

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    Integer postCounts = questionPostRepository.countByCreatedDateAfter(yesterday);

    // 24시간 이내에 작성된 모든 질문글 일간 점수 업데이트
    Pageable pageable = PageRequest.of(0, Math.max(postCounts, 1));
    Page<QuestionPost> posts = questionPostRepository.findAllByCreatedDateAfter(yesterday, pageable);

    // 일간 점수 업데이트
    for (QuestionPost post : posts) {
      post.updateDailyScore(scoreCalculator.calculateQuestionPostDailyScore(post));
      questionPostRepository.save(post);
    }

    // 일간 인기 질문글 내역 초기화 후 상위 30개 글 저장
    popularPostRepository.deleteAllByContentTypeAndPopularType(QUESTION, DAILY);
    pageable = PageRequest.of(
        0,
        SAVE_POPULAR_POST_COUNT,
        Sort.by("dailyScore").descending()
    );
    posts = questionPostRepository.findAllByCreatedDateAfter(yesterday, pageable);
    for (QuestionPost post : posts) {
      popularPostRepository.save(PopularPost.builder()
          .postId(post.getQuestionPostId())
          .contentType(QUESTION)
          .popularType(DAILY)
          .build());
    }
  }

  // 6시간마다 주간 인기 질문글 점수 계산 후 상위 30개 데이터 저장
  @Async
  @Transactional
  @Scheduled(fixedRate = QUESTION_WEEKLY_SCHEDULED_RATE) // 6시간마다 실행
  public void calculateQuestionWeeklyScore() {

    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    Integer postCounts = questionPostRepository.countByCreatedDateAfter(lastWeek);

    // 7일 이내에 작성된 모든 질문글 주간 점수 업데이트
    Pageable pageable = PageRequest.of(0, Math.max(postCounts, 1));
    Page<QuestionPost> posts = questionPostRepository.findAllByCreatedDateAfter(lastWeek, pageable);

    // 주간 점수 업데이트
    for (QuestionPost post : posts) {
      post.updateWeeklyScore(scoreCalculator.calculateQuestionPostWeeklyScore(post));
      questionPostRepository.save(post);
    }

    // 주간 인기 질문글 내역 초기화 후 상위 30개 글 저장
    popularPostRepository.deleteAllByContentTypeAndPopularType(QUESTION, WEEKLY);
    pageable = PageRequest.of(
        0,
        SAVE_POPULAR_POST_COUNT,
        Sort.by("weeklyScore").descending()
    );
    posts = questionPostRepository.findAllByCreatedDateAfter(lastWeek, pageable);
    for (QuestionPost post : posts) {
      popularPostRepository.save(PopularPost.builder()
          .postId(post.getQuestionPostId())
          .contentType(QUESTION)
          .popularType(WEEKLY)
          .build());
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

    // 일간 인기 자료글 내역 초기화 후 상위 30개 글 저장
    popularPostRepository.deleteAllByContentTypeAndPopularType(DOCUMENT, DAILY);
    Pageable pageable = PageRequest.of(
        0,
        SAVE_POPULAR_POST_COUNT,
        Sort.by("dailyScore").descending()
    );
    Page<DocumentPost> topPosts = documentPostRepository.findAll(pageable);
    // 상위 30개 글 isPopular = true 설정 후 저장
    for (DocumentPost post : topPosts) {
      post.setIsPopular(true);
      popularPostRepository.save(PopularPost.builder()
          .postId(post.getDocumentPostId())
          .contentType(DOCUMENT)
          .popularType(DAILY)
          .build());
    }

    // isPopular = true 변경 저장
    documentPostRepository.saveAll(topPosts);
  }

  /**
   * <h3>매주 월요일 자정에 주간 인기 자료글 업데이트</h3>
   * <p>자료 글 일간 인기점수 초기화</p>
   * <p>인기글 계산 로직에 따라 주간 인기점수 계산</p>
   * <p>인기글에 등록 된 상위 10개 글 isPopular = true 변경</p>
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

    // 주간 인기 자료글 내역 초기화 후 상위 30개 글 저장
    popularPostRepository.deleteAllByContentTypeAndPopularType(DOCUMENT, WEEKLY);
    Pageable pageable = PageRequest.of(
        0,
        SAVE_POPULAR_POST_COUNT,
        Sort.by("weeklyScore").descending()
    );
    Page<DocumentPost> topPosts = documentPostRepository.findAll(pageable);

    // 상위 30개 글 isPopular = true 설정 후 저장
    for (DocumentPost post : topPosts) {
      post.setIsPopular(true);
      popularPostRepository.save(PopularPost.builder()
          .postId(post.getDocumentPostId())
          .contentType(DOCUMENT)
          .popularType(WEEKLY)
          .build());
    }

    // isPopular = true 변경 저장
    documentPostRepository.saveAll(topPosts);
  }

  /**
   * <h3>일간 인기 질문 글 조회 로직
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>24시간 이내에 작성 된 질문 글을 대상으로 조회합니다.</p>
   * <p>일간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getDailyPopularQuestionPosts(QuestionCommand command) {

    // 페이지 설정
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize()
    );

    Page<PopularPost> popularPostsPage = popularPostRepository
        .findAllByContentTypeAndPopularType(QUESTION, DAILY, pageable);

    // 일간 인기 질문 글의 postId
    List<UUID> postIds = popularPostsPage.stream()
        .map(PopularPost::getPostId)
        .collect(Collectors.toList());

    // 질문 글 조회
    List<QuestionPost> questionPosts = questionPostRepository.findAllById(postIds);

    // 내림차순 정렬
    questionPosts.sort(Comparator.comparing(QuestionPost::getDailyScore).reversed());

    pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("dailyScore").descending()
    );

    Page<QuestionPost> questionPostPage = new PageImpl<>(
        questionPosts,
        pageable,
        popularPostsPage.getTotalElements());

    return QuestionDto.builder()
        .questionPostsPage(questionPostPage)
        .build();
  }

  /**
   * <h3>주간 인기 질문글 조회 로직
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>7일 이내에 작성 된 질문 글을 대상으로 조회합니다.</p>
   * <p>주간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public QuestionDto getWeeklyPopularQuestionPosts(QuestionCommand command) {

    // 페이지 설정
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize()
    );

    Page<PopularPost> popularPostsPage = popularPostRepository
        .findAllByContentTypeAndPopularType(QUESTION, WEEKLY, pageable);

    // 일간 인기 질문 글의 postId
    List<UUID> postIds = popularPostsPage.stream()
        .map(PopularPost::getPostId)
        .collect(Collectors.toList());

    // 질문 글 조회
    List<QuestionPost> questionPosts = questionPostRepository.findAllById(postIds);

    // 내림차순 정렬
    questionPosts.sort(Comparator.comparing(QuestionPost::getWeeklyScore).reversed());

    pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("weeklyScore").descending()
    );

    Page<QuestionPost> questionPostPage = new PageImpl<>(
        questionPosts,
        pageable,
        popularPostsPage.getTotalElements());

    return QuestionDto.builder()
        .questionPostsPage(questionPostPage)
        .build();
  }

  /**
   * <h3>일간 인기 자료글 조회 로직</h3>
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>일간 자료 글 인기점수는 24시간마다 초기화됩니다.</p>
   * <p>일간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public DocumentDto getDailyPopularDocumentPosts(DocumentCommand command) {

    // 페이지 설정
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize()
    );

    Page<PopularPost> popularPostsPage = popularPostRepository
        .findAllByContentTypeAndPopularType(DOCUMENT, DAILY, pageable);

    // 일간 인기 질문 글의 postId
    List<UUID> postIds = popularPostsPage.stream()
        .map(PopularPost::getPostId)
        .collect(Collectors.toList());

    // 질문 글 조회
    List<DocumentPost> documentPosts = documentPostRepository.findAllById(postIds);

    // 내림차순 정렬
    documentPosts.sort(Comparator.comparing(DocumentPost::getDailyScore).reversed());

    pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("dailyScore").descending()
    );

    Page<DocumentPost> documentPostsPage = new PageImpl<>(
        documentPosts,
        pageable,
        popularPostsPage.getTotalElements());

    return DocumentDto.builder()
        .documentPostsPage(documentPostsPage)
        .build();
  }

  /**
   * <h3>주간 인기 자료글 조회 로직</h3>
   * <p>PopularPost 테이블에 있는 30개의 데이터를 반환합니다.</p>
   * <p>주간 자료 글 인기점수는 7일마다 초기화됩니다.
   * <p>주간 인기 점수가 높은 순으로 조회합니다.</p>
   *
   * @param command pageNumber, pageSize
   * @return
   */
  @Transactional
  public DocumentDto getWeeklyPopularDocumentPosts(DocumentCommand command) {

    // 페이지 설정
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize()
    );

    Page<PopularPost> popularPostsPage = popularPostRepository
        .findAllByContentTypeAndPopularType(DOCUMENT, WEEKLY, pageable);

    // 일간 인기 질문 글의 postId
    List<UUID> postIds = popularPostsPage.stream()
        .map(PopularPost::getPostId)
        .collect(Collectors.toList());

    // 질문 글 조회
    List<DocumentPost> documentPosts = documentPostRepository.findAllById(postIds);

    // 내림차순 정렬
    documentPosts.sort(Comparator.comparing(DocumentPost::getWeeklyScore).reversed());

    pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by("weeklyScore").descending()
    );

    Page<DocumentPost> documentPostsPage = new PageImpl<>(
        documentPosts,
        pageable,
        popularPostsPage.getTotalElements());

    return DocumentDto.builder()
        .documentPostsPage(documentPostsPage)
        .build();
  }
}
