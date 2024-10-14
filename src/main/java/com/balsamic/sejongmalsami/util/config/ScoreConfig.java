package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.object.DocumentPost;
import com.balsamic.sejongmalsami.object.QuestionPost;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScoreConfig {

  // 질문글 일간 점수 계산 로직
  public Integer calculateQuestionPostDailyScore(QuestionPost post) {
    return post.getAnswerCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

  // 질문글 주간 점수 계산 로직
  public Integer calculateQuestionPostWeeklyScore(QuestionPost post) {
    return post.getAnswerCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

  // 자료글 일간 점수 계산 로직
  public Integer calculateDocumentPostDailyScore(DocumentPost post) {
    return post.getCommentCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

  // 자료글 주간 점수 계산 로직
  public Integer calculateDocumentPostWeeklyScore(DocumentPost post) {
    return post.getCommentCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }
}
