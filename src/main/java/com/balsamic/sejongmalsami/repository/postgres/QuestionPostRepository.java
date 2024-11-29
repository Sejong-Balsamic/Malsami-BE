package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
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

  // createdDate 이후에 작성 된 인기글 상위 n개 조회
  Page<QuestionPost> findAllByCreatedDateAfter(LocalDateTime createdDate, Pageable pageable);

  // 아직 답변하지 않은 질문글 조회 및 단과대 필터링 (최신순)
  @Query("""
        select q
        from QuestionPost q
        where (:faculty is null or :faculty member of q.faculties)
        and (q.answerCount = 0)
        """)
  Page<QuestionPost> findNotAnsweredQuestionByFilter(
      @Param("faculty") Faculty faculty,
      Pageable pageable);

  // 과목 및 채택 상태 필터링
  @Query("""
        select distinct q
        from QuestionPost q
        left join q.questionPresetTags qt
        where
            (:subject is null or q.subject = :subject)
            and (:faculty is null or :faculty member of q.faculties)
            and (:questionPresetTags is null or qt in :questionPresetTags)
            and (
                :chaetaekStatus = 'ALL'
                or (:chaetaekStatus = 'CHAETAEK' and q.chaetaekStatus = true)
                or (:chaetaekStatus = 'NO_CHAETAEK' and q.chaetaekStatus = false)
            )
        """)
  Page<QuestionPost> findQuestionPostsByFilter(
      @Param("subject") String subject,
      @Param("faculty") Faculty faculty,
      @Param("questionPresetTags") List<QuestionPresetTag> questionPresetTags,
      @Param("chaetaekStatus") String chaetaekStatus,
      Pageable pageable);

  // 검색
  @Query(
      value = "SELECT DISTINCT p.* FROM question_post p " +
              "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
              "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
              "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) ",
      countQuery = "SELECT COUNT(DISTINCT p.question_post_id) FROM question_post p " +
                   "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                   "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) ",
      nativeQuery = true
  )
  Page<QuestionPost> findQuestionPostsByQuery(
      @Param("query") String query,
      @Param("subject") String subject,
      Pageable pageable
  );

  List<QuestionPost> findByMember(Member member);

  Long countByMember(Member member);
}
