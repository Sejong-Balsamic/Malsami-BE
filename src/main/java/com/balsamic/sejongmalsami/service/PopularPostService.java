package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentPost;
import com.balsamic.sejongmalsami.object.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PopularPostService {

  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;


  // 매일 자정마다 일간 인기글 점수 계산
  @Scheduled(cron = "0 0 0 * * ?")
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
  }

  // 매주 월요일 자정마다 주간 인기글 점수 계산
  @Scheduled(cron = "0 0 0 * * MON")
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
  }

  // 점수 계산 (답변수 * 3 + 좋아요수 * 2 + 조회수)
  private Integer calculateQuestionPostScore(QuestionPost post) {
    return post.getAnswerCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

  // 점수 계산 (다운수 * 3 + 좋아요수 * 2 + 조회수)
  private Integer calculateDocumentPostScore(DocumentPost post) {
    return post.getDownloadCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

}
