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
  public int calculateQuestionPostDailyScore(QuestionPost post) {
    return post.getViewCount() * scoreConfig.getQuestionDailyViewCountWeight()
        + post.getLikeCount() * scoreConfig.getQuestionDailyLikeCountWeight()
        + post.getAnswerCount() * scoreConfig.getQuestionDailyAnswerCountWeight();
  }

  // 질문 글 주간 점수 로직
  public int calculateQuestionPostWeeklyScore(QuestionPost post) {
    return post.getViewCount() * scoreConfig.getQuestionWeeklyViewCountWeight()
        + post.getLikeCount() * scoreConfig.getQuestionWeeklyLikeCountWeight()
        + post.getAnswerCount() * scoreConfig.getQuestionWeeklyAnswerCountWeight();
  }

  // 자료 글 일간 점수 로직
  // TODO: 자료 다운로드 수 로직에 추가해야 합니다.
  public int calculateDocumentPostDailyScore(DocumentPost post) {
    return post.getViewCount() * scoreConfig.getDocumentDailyViewCountWeight()
        + post.getLikeCount() * scoreConfig.getDocumentDailyLikeCountWeight()
        + post.getDislikeCount() * scoreConfig.getDocumentDailyDislikeCountWeight();
  }

  // 자료 글 주간 점수 로직
  // TODO: 자료 다운로드 수 로직에 추가해야 합니다.
  public int calculateDocumentPostWeeklyScore(DocumentPost post) {
    return post.getViewCount() * scoreConfig.getDocumentWeeklyViewCountWeight()
        + post.getLikeCount() * scoreConfig.getDocumentWeeklyLikeCountWeight()
        + post.getDislikeCount() * scoreConfig.getDocumentWeeklyDislikeCountWeight();
  }
}
