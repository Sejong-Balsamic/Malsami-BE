package com.balsamic.sejongmalsami.repository.postgres;

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
  Optional<List<QuestionPost>> findAllByCreatedDateAfter(LocalDateTime startDate);

  // 아직 답변하지 않은 질문글 조회 및 단과대 필터링 (최신순)
  @Query("""
      select q
      from QuestionPost q
      where (:faculty is null or :faculty member of q.faculties)
      and (q.answerCount = 0)
      """)
  public Page<QuestionPost> findNotAnsweredQuestionByFilter(
      @Param("faculty") String faculty,
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
          and (:isRewardYeopjeonRequest = false or q.rewardYeopjeon > 0)
      """)
  Page<QuestionPost> findQuestionPostsByFilter(
      @Param("subject") String subject,
      @Param("faculty") String faculty,
      @Param("questionPresetTags") List<QuestionPresetTag> questionPresetTags,
      @Param("chaetaekStatus") String chaetaekStatus,
      @Param("isRewardYeopjeonRequest") Boolean isRewardYeopjeonRequest,
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

  // 내가 작성한 질문글
  Page<QuestionPost> findAllByMember(Member member, Pageable pageable);

  // 내가 답변을 작성한 질문글
  @Query("""
      select distinct q
      from QuestionPost q
      join AnswerPost a on a.questionPost.questionPostId = q.questionPostId
      where a.member = :member
      """)
  Page<QuestionPost> findAllByAnsweredByMember(
      @Param("member") Member member,
      Pageable pageable
  );

  Long countByMember(Member member);

  @Query("""
    SELECT DISTINCT q
    FROM QuestionPost q
         LEFT JOIN q.questionPresetTags qt
    WHERE
        (:query IS NULL 
            OR LOWER(q.title) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
            OR LOWER(q.content) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
        AND (:subject IS NULL 
            OR LOWER(q.subject) LIKE LOWER(CONCAT('%', CAST(:subject AS string), '%')))
        AND (:faculty IS NULL 
            OR EXISTS (
                SELECT 1
                FROM QuestionPost qp, IN(qp.faculties) f
                WHERE qp = q AND LOWER(f) LIKE LOWER(CONCAT('%', CAST(:faculty AS string), '%'))
            ))
        AND (
            :chaetaekStatus = 'ALL'
            OR (:chaetaekStatus = 'CHAETAEK' AND q.chaetaekStatus = true)
            OR (:chaetaekStatus = 'NO_CHAETAEK' AND q.chaetaekStatus = false)
        )
        AND (:questionPresetTags IS NULL 
            OR qt IN :questionPresetTags)
    """)
  Page<QuestionPost> findAllDynamicQuestionPosts(
      @Param("query") String query,
      @Param("subject") String subject,
      @Param("faculty") String faculty,
      @Param("chaetaekStatus") String chaetaekStatus,
      @Param("questionPresetTags") List<QuestionPresetTag> questionPresetTags,
      Pageable pageable
  );
}
