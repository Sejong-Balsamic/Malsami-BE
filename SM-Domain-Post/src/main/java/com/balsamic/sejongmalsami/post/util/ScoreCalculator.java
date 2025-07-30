package com.balsamic.sejongmalsami.post.util;

import com.balsamic.sejongmalsami.post.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.util.properties.ScoreProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScoreCalculator {

  private final ScoreProperties scoreProperties;
  private final DocumentFileRepository documentFileRepository;

  // 질문 글 일간 점수 로직
  public long calculateQuestionPostDailyScore(QuestionPost post) {
    return (long) post.getViewCount() * scoreProperties.getQuestionDailyViewCountWeight()
        + (long) post.getLikeCount() * scoreProperties.getQuestionDailyLikeCountWeight()
        + (long) post.getAnswerCount() * scoreProperties.getQuestionDailyAnswerCountWeight();
  }

  // 질문 글 주간 점수 로직
  public long calculateQuestionPostWeeklyScore(QuestionPost post) {
    return (long) post.getViewCount() * scoreProperties.getQuestionWeeklyViewCountWeight()
        + (long) post.getLikeCount() * scoreProperties.getQuestionWeeklyLikeCountWeight()
        + (long) post.getAnswerCount() * scoreProperties.getQuestionWeeklyAnswerCountWeight();
  }

  // 자료 글 일간 점수 로직
  public long calculateDocumentPostDailyScore(DocumentPost post) {
    long totalDailyDownloadCount = 0L;
    List<DocumentFile> documentFiles =
        documentFileRepository.findByDocumentPost_DocumentPostId(post.getDocumentPostId());
    // 인기글 최소 좋아요
    if (post.getLikeCount() < scoreProperties.getDocumentMinimumLikeCount()) {
      return 0;
    }

    // 자료파일의 다운로드 수 총합 계산
    for(DocumentFile file : documentFiles){
      totalDailyDownloadCount += file.getDailyDownloadCount() != null ? file.getDailyDownloadCount() : 0;
    }

    // 점수 총합 계산
    return (long) post.getViewCount() * scoreProperties.getDocumentDailyViewCountWeight()
        + (long) post.getLikeCount() * scoreProperties.getDocumentDailyLikeCountWeight()
        + (long) post.getDislikeCount() * scoreProperties.getDocumentDailyDislikeCountWeight()
        + totalDailyDownloadCount * scoreProperties.getDocumentDailyDownloadCountWeight();
  }

  // 자료 글 주간 점수 로직
  public long calculateDocumentPostWeeklyScore(DocumentPost post) {
    long totalWeeklyDownloadCount = 0L;
    List<DocumentFile> documentFiles =
        documentFileRepository.findByDocumentPost_DocumentPostId(post.getDocumentPostId());
    // 인기글 최소 좋아요 수
    if (post.getLikeCount() < scoreProperties.getDocumentMinimumLikeCount()) {
      return 0;
    }

    // 자료파일의 다운로드 수 총합 계산
    for(DocumentFile file : documentFiles){
      totalWeeklyDownloadCount += file.getWeeklyDownloadCount() != null ? file.getWeeklyDownloadCount() : 0;
    }

    return (long) post.getViewCount() * scoreProperties.getDocumentWeeklyViewCountWeight()
        + (long) post.getLikeCount() * scoreProperties.getDocumentWeeklyLikeCountWeight()
        + (long) post.getDislikeCount() * scoreProperties.getDocumentWeeklyDislikeCountWeight()
        + totalWeeklyDownloadCount * scoreProperties.getDocumentWeeklyDownloadCountWeight();
  }
}
