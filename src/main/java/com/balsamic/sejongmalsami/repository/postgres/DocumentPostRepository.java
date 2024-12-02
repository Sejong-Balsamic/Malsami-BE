package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentPostRepository extends JpaRepository<DocumentPost, UUID> {

  // 모든 자료 글 dailyScore 초기화
  @Modifying
  @Query("UPDATE DocumentPost p SET p.dailyScore = 0")
  void resetDailyScore();

  // 모든 자료 글 weeklyScore 초기화
  @Modifying
  @Query("UPDATE DocumentPost p SET p.weeklyScore = 0")
  void resetWeeklyScore();

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
//          "AND (:documentTypes IS NULL OR dt.document_type IN (:documentTypes)) " +
//          "ORDER BY p.createdDate DESC " +
//          "FETCH FIRST ? rows only",
//      countQuery = "SELECT COUNT(DISTINCT p.document_post_id) FROM document_post p " +
//          "LEFT JOIN document_post_document_types dt ON p.document_post_id = dt.document_post_document_post_id " +
//          "WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
//          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) " +
//          "AND (:content IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))) " +
//          "AND (:documentTypes IS NULL OR dt.document_type IN (:documentTypes))",
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

  List<DocumentPost> findByMember(Member member);

  long countByMember(Member member);

  long countByMemberAndIsPopularTrue(Member member);
}
