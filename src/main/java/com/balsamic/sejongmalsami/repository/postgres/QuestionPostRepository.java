package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionPostRepository extends JpaRepository<QuestionPost, UUID> {

  // 특정 답변이 달린 질문글 반환
  @Query("SELECT a.questionPost FROM AnswerPost a WHERE a = :answerPost")
  Optional<QuestionPost> findQuestionPostByAnswerPost(@Param("answerPost") AnswerPost answerPost);

  // 글 작성일이 startDate 보다 나중인 질문 게시글 개수
  Integer countByCreatedDateAfter(LocalDateTime startDate);

  // 글 작성일이 startDate 보다 나중인 질문 게시글
  Page<QuestionPost> findByCreatedDateAfter(LocalDateTime startDate, Pageable pageable);

  // 일간 인기글 상위 n개 조회 (24시간 이내에 등록된 글만 조회)
  List<QuestionPost> findTop50ByCreatedDateAfterOrderByDailyScoreDesc(LocalDateTime yesterday);

  // 주간 인기글 상위 n개 조회 (7일 이내에 등록된 글만 조회)
  List<QuestionPost> findTop50ByCreatedDateAfterOrderByWeeklyScoreDesc(LocalDateTime lastWeek);

  // 아직 답변하지 않은 질문글 조회 (최신순)
  Page<QuestionPost> findByAnswerCountOrderByCreatedDateDesc(int answerCount, Pageable pageable);

//  // 질문글 필터링
//  @Query("SELECT q FROM QuestionPost q " +
//      "LEFT JOIN AnswerPost a ON a.questionPost.questionPostId = q.questionPostId " +
//      "WHERE (:#{#command.subject} IS NULL OR q.subject = :#{#command.subject}) " +
//      "AND (:#{#command.minYeopjeon} IS NULL OR q.rewardYeopjeon >= :#{#command.minYeopjeon}) " +
//      "AND (:#{#command.maxYeopjeon} IS NULL OR q.rewardYeopjeon <= :#{#command.maxYeopjeon}) " +
//      "AND (" +
//      "    (:#{#command.questionPresetTagSet.size()} = 1 AND :#{#command.questionPresetTagSet} MEMBER OF q.questionPresetTagSet) " +
//      "    OR (:#{#command.questionPresetTagSet.size()} = 2 AND q.questionPresetTagSet IS NOT EMPTY " +
//      "        AND FUNCTION('array_contains_all', q.questionPresetTagSet, :#{#command.questionPresetTagSet}) = true)" +
//      ") " +
//      "AND (COUNT(a) = 0 OR SUM(CASE WHEN a.isChaetaek = true THEN 1 ELSE 0 END) = 0) " +
//      "GROUP BY q " +
//      "ORDER BY " +
//      "CASE WHEN :#{#command.sortType} = 'LATEST' THEN q.createdDate END DESC, " +
//      "CASE WHEN :#{#command.sortType} = 'MOST_LIKED' THEN q.likeCount END DESC, " +
//      "CASE WHEN :#{#command.sortType} = 'YEOPJEON_REWARD' THEN q.rewardYeopjeon END DESC, " +
//      "CASE WHEN :#{#command.sortType} = 'VIEW_COUNT' THEN q.viewCount END DESC")
//  Page<QuestionPost> findFilteredQuestions(QuestionCommand command, Pageable pageable);
}