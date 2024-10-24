package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionPostRepository extends JpaRepository<QuestionPost, UUID> {

  // 글 작성일이 startDate 보다 나중인 질문 게시글 개수
  Integer countByCreatedDateAfter(LocalDateTime startDate);

  // 글 작성일이 startDate 보다 나중인 질문 게시글
  Page<QuestionPost> findByCreatedDateAfter(LocalDateTime startDate, Pageable pageable);

  // 일간 인기글 상위 n개 조회 (24시간 이내에 등록된 글만 조회)
  Page<QuestionPost> findByCreatedDateAfterOrderByDailyScoreDesc(LocalDateTime yesterday, Pageable pageable);

  // 주간 인기글 상위 n개 조회
  Page<QuestionPost> findByCreatedDateAfterOrderByWeeklyScoreDesc(LocalDateTime lastWeek, Pageable pageable);
}