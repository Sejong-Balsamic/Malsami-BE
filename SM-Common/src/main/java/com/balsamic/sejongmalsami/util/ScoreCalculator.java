package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.postgres.DocumentFile;
import com.balsamic.sejongmalsami.postgres.DocumentPost;
import com.balsamic.sejongmalsami.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.util.config.ScoreConfig;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreCalculator {

  private final ScoreConfig scoreConfig;
  private final DocumentFileRepository documentFileRepository;

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
  public long calculateDocumentPostDailyScore(DocumentPost post) {
    long totalDailyDownloadCount = 0L;
    List<DocumentFile> documentFiles =
        documentFileRepository.findByDocumentPost_DocumentPostId(post.getDocumentPostId());
    // 인기글 최소 좋아요
    if (post.getLikeCount() < scoreConfig.getDocumentMinimumLikeCount()) {
      return 0;
    }

    // 자료파일의 다운로드 수 총합 계산
    for(DocumentFile file : documentFiles){
      totalDailyDownloadCount += file.getDailyDownloadCount() != null ? file.getDailyDownloadCount() : 0;
    }

    // 점수 총합 계산
    return (long) post.getViewCount() * scoreConfig.getDocumentDailyViewCountWeight()
        + (long) post.getLikeCount() * scoreConfig.getDocumentDailyLikeCountWeight()
        + (long) post.getDislikeCount() * scoreConfig.getDocumentDailyDislikeCountWeight()
        + totalDailyDownloadCount * scoreConfig.getDocumentDailyDownloadCountWeight();
  }

  // 자료 글 주간 점수 로직
  public long calculateDocumentPostWeeklyScore(DocumentPost post) {
    long totalWeeklyDownloadCount = 0L;
    List<DocumentFile> documentFiles =
        documentFileRepository.findByDocumentPost_DocumentPostId(post.getDocumentPostId());
    // 인기글 최소 좋아요 수
    if (post.getLikeCount() < scoreConfig.getDocumentMinimumLikeCount()) {
      return 0;
    }

    // 자료파일의 다운로드 수 총합 계산
    for(DocumentFile file : documentFiles){
      totalWeeklyDownloadCount += file.getWeeklyDownloadCount() != null ? file.getWeeklyDownloadCount() : 0;
    }

    return (long) post.getViewCount() * scoreConfig.getDocumentWeeklyViewCountWeight()
        + (long) post.getLikeCount() * scoreConfig.getDocumentWeeklyLikeCountWeight()
        + (long) post.getDislikeCount() * scoreConfig.getDocumentWeeklyDislikeCountWeight()
        + totalWeeklyDownloadCount * scoreConfig.getDocumentWeeklyDownloadCountWeight();
  }
}
