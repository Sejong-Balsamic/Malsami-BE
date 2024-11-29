package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.util.config.ScoreConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreCalculator {

  private final ScoreConfig scoreConfig;

  // 질문 글 일간 점수 로직
  public long calculateQuestionPostDailyScore(QuestionPost post) {
    return (long) post.getViewCount() * scoreConfig.getQuestionDailyViewCountWeight()
        + (long) post.getLikeCount() * scoreConfig.getQuestionDailyLikeCountWeight()
        + (long) post.getAnswerCount() * scoreConfig.getQuestionDailyAnswerCountWeight();
  }

  // 질문 글 주간 점수 로직
  public long calculateQuestionPostWeeklyScore(QuestionPost post) {
    return (long) post.getViewCount() * scoreConfig.getQuestionWeeklyViewCountWeight()
        + (long) post.getLikeCount() * scoreConfig.getQuestionWeeklyLikeCountWeight()
        + (long) post.getAnswerCount() * scoreConfig.getQuestionWeeklyAnswerCountWeight();
  }

  // 자료 글 일간 점수 로직
  // TODO: 자료 다운로드 수 로직에 추가해야 합니다.
  public long calculateDocumentPostDailyScore(DocumentPost post) {
    // 인기글 최소 좋아요 수
    if (post.getLikeCount() < scoreConfig.getDocumentMinimumLikeCount()) {
      return 0;
    }
    return (long) post.getViewCount() * scoreConfig.getDocumentDailyViewCountWeight()
        + (long) post.getLikeCount() * scoreConfig.getDocumentDailyLikeCountWeight()
        + (long) post.getDislikeCount() * scoreConfig.getDocumentDailyDislikeCountWeight();
  }

  // 자료 글 주간 점수 로직
  // TODO: 자료 다운로드 수 로직에 추가해야 합니다.
  public long calculateDocumentPostWeeklyScore(DocumentPost post) {
    // 인기글 최소 좋아요 수
    if (post.getLikeCount() < scoreConfig.getDocumentMinimumLikeCount()) {
      return 0;
    }
    return (long) post.getViewCount() * scoreConfig.getDocumentWeeklyViewCountWeight()
        + (long) post.getLikeCount() * scoreConfig.getDocumentWeeklyLikeCountWeight()
        + (long) post.getDislikeCount() * scoreConfig.getDocumentWeeklyDislikeCountWeight();
  }
}
