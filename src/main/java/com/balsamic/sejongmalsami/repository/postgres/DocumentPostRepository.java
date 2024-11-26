package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentPostRepository extends JpaRepository<DocumentPost, UUID> {

  // 일간 인기글 계산 (글 작성일이 startDate 보다 나중인 질문 게시글)
  @Query("SELECT p FROM DocumentPost p WHERE p.createdDate >= :startDate")
  List<DocumentPost> findDocumentPostsAfter(LocalDateTime startDate);

  // 일간 인기글 페이징 조회 (dailyScore 기준 내림차순)
  Page<DocumentPost> findByCreatedDateAfterOrderByDailyScoreDesc(LocalDateTime startDate, Pageable pageable);

  // 주간 인기글 페이징 조회 (weeklyScore 기준 내림차순)
  Page<DocumentPost> findByCreatedDateAfterOrderByWeeklyScoreDesc(LocalDateTime startDate, Pageable pageable);

  // 자료 글 교과목명, 태그 필터링
  @Query("SELECT DISTINCT p FROM DocumentPost p " +
         "LEFT JOIN p.documentTypes dt " +
         "WHERE " +
         "(:subject IS NULL OR p.subject = :subject) " +
         "AND (:documentTypes IS NULL OR dt IN :documentTypes)" +
         "AND (:faculty IS NULL OR :faculty MEMBER OF p.faculties)" +
         "AND(:postTier IS NULL OR p.postTier = :postTier)")
  Page<DocumentPost> findDocumentPostsByFilter(
      @Param("subject") String subject,
      @Param("documentTypes") List<DocumentType> documentTypes,
      @Param("faculty") Faculty faculty,
      @Param("postTier") PostTier postTier,
      Pageable pageable
  );

//  // 검색
//  @Query(
//      value = "SELECT DISTINCT p.* FROM document_post p " +
//          "LEFT JOIN document_post_document_types dt ON p.document_post_id = dt.document_post_document_post_id " +
//          "WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
//          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) " +
//          "AND (:content IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))) " +
//          "AND (:documentTypes IS NULL OR dt.document_types IN (:documentTypes)) " +
//          "ORDER BY p.created_date DESC " +
//          "FETCH FIRST ? rows only",
//      countQuery = "SELECT COUNT(DISTINCT p.document_post_id) FROM document_post p " +
//          "LEFT JOIN document_post_document_types dt ON p.document_post_id = dt.document_post_document_post_id " +
//          "WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
//          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) " +
//          "AND (:content IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))) " +
//          "AND (:documentTypes IS NULL OR dt.document_types IN (:documentTypes))",
//      nativeQuery = true
//  )
//  Page<DocumentPost> findDocumentPostsByFilter(
//      @Param("title") String title,
//      @Param("subject") String subject,
//      @Param("content") String content,
//      @Param("documentTypes") List<String> documentTypes,
//      Pageable pageable
//  );

  // 검색
  @Query(
      value = "SELECT DISTINCT p.* FROM document_post p " +
              "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
              "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
              "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) ",
      countQuery = "SELECT COUNT(DISTINCT p.document_post_id) FROM document_post p " +
                   "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
                   "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) ",
      nativeQuery = true
  )
  Page<DocumentPost> findDocumentPostsByQuery(
      @Param("query") String query,
      @Param("subject") String subject,
      Pageable pageable
  );
}
