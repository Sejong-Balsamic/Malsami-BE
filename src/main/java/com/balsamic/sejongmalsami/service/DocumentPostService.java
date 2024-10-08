package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentPost;
import com.balsamic.sejongmalsami.object.DocumentPostCommand;
import com.balsamic.sejongmalsami.object.DocumentPostDto;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentPostService {

  private final DocumentPostRepository documentPostRepository;
  private final MemberRepository memberRepository;

  // 질문 게시글 등록
  public DocumentPostDto saveDocumentPost(DocumentPostCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    DocumentPost documentPost = DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .postTier(PostTier.CHEONMIN)
        .likeCount(0)
        .dislikeCount(0)
        .downloadCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(false)
        .dailyScore(0)
        .weeklyScore(0)
        .build();

    return DocumentPostDto.builder()
        .documentPost(documentPostRepository.save(documentPost))
        .build();
  }

  // 매일 자정마다 일간 인기글 점수 계산
  @Scheduled(cron = "0 0 0 * * ?")
  public void calculateDailyScore() {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    List<DocumentPost> posts = documentPostRepository.findDocumentPostsAfter(yesterday);

    for (DocumentPost post : posts) {
      post.updateDailyScore(calculateScore(post));
      documentPostRepository.save(post);
    }
  }

  // 매주 월요일 자정마다 주간 인기글 점수 계산
  @Scheduled(cron = "0 0 0 * * MON")
  public void calculateWeeklyScore() {
    LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
    List<DocumentPost> posts = documentPostRepository.findDocumentPostsAfter(lastWeek);

    for (DocumentPost post : posts) {
      post.updateWeeklyScore(calculateScore(post));
      documentPostRepository.save(post);
    }
  }

  // 점수 계산 (다운수 * 3 + 좋아요수 * 2 + 조회수)
  private Integer calculateScore(DocumentPost post) {
    return post.getDownloadCount() * 3 + post.getLikeCount() * 2 + post.getViewCount();
  }

}
