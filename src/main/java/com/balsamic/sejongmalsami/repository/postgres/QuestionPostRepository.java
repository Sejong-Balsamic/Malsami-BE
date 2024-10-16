package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionPostRepository extends JpaRepository<QuestionPost, UUID> {

  // 글 작성일이 startDate 보다 나중인 질문 게시글
  @Query("SELECT p FROM QuestionPost p WHERE p.createdDate >= :startDate")
  List<QuestionPost> findQuestionPostsAfter(LocalDateTime startDate);

  // 일간 인기글 상위 30개 조회 (24시간 이내에 등록된 글만 조회)
  @Query("SELECT q FROM QuestionPost q WHERE q.createdDate > :yesterday ORDER BY q.dailyScore DESC")
  List<QuestionPost> findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(@Param("yesterday") LocalDateTime yesterday);

  // 주간 인기글 상위 30개 조회
  @Query("SELECT q FROM QuestionPost q WHERE q.createdDate > :lastWeek ORDER BY q.weeklyScore DESC")
  List<QuestionPost> findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(@Param("lastWeek") LocalDateTime lastWeek);
}