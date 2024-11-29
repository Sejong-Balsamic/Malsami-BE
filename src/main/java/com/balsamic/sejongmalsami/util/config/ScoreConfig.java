package com.balsamic.sejongmalsami.util.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ScoreConfig {

  // 질문 글 일간 점수 가중치
  @Value("${score.question.daily.view-count-weight}")
  private int questionDailyViewCountWeight;

  @Value("${score.question.daily.like-count-weight}")
  private int questionDailyLikeCountWeight;

  @Value("${score.question.daily.answer-count-weight}")
  private int questionDailyAnswerCountWeight;

  // 질문 글 주간 점수 가중치
  @Value("${score.question.weekly.view-count-weight}")
  private int questionWeeklyViewCountWeight;

  @Value("${score.question.weekly.like-count-weight}")
  private int questionWeeklyLikeCountWeight;

  @Value("${score.question.weekly.answer-count-weight}")
  private int questionWeeklyAnswerCountWeight;

  // 자료 글 일간 점수 가중치
  @Value("${score.document.daily.view-count-weight}")
  private int documentDailyViewCountWeight;

  @Value("${score.document.daily.like-count-weight}")
  private int documentDailyLikeCountWeight;

  @Value("${score.document.daily.dislike-count-weight}")
  private int documentDailyDislikeCountWeight;

  @Value("${score.document.daily.download-count-weight}")
  private int documentDailyDownloadCountWeight;

  // 자료 글 주간 점수 가중치
  @Value("${score.document.weekly.view-count-weight}")
  private int documentWeeklyViewCountWeight;

  @Value("${score.document.weekly.like-count-weight}")
  private int documentWeeklyLikeCountWeight;

  @Value("${score.document.weekly.dislike-count-weight}")
  private int documentWeeklyDislikeCountWeight;

  @Value("${score.document.weekly.download-count-weight}")
  private int documentWeeklyDownloadCountWeight;

  @Value("${score.document.minimum-like-count}")
  private int documentMinimumLikeCount;
}
