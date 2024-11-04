package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

  // 일간 인기글 상위 30개 조회
  @Query("SELECT d FROM DocumentPost d WHERE d.createdDate > :yesterday ORDER BY d.dailyScore DESC")
  List<DocumentPost> findTop30ByOrderByDailyScoreDescAndCreatedDateAfter(@Param("yesterday") LocalDateTime yesterday);

  // 주간 인기글 상위 30개 조회
  @Query("SELECT d FROM DocumentPost d WHERE d.createdDate > :lastWeek ORDER BY d.weeklyScore DESC")
  List<DocumentPost> findTop30ByOrderByWeeklyScoreDescAndCreatedDateAfter(@Param("lastWeek") LocalDateTime lastWeek);

//  @Query("SELECT DISTINCT p FROM DocumentPost p LEFT JOIN p.documentTypeSet t " +
//      "WHERE (:#{#command.title} IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :#{#command.title}, '%'))) " +
//      "AND (:#{#command.subject} IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :#{#command.subject}, '%'))) " +
//      "AND (:#{#command.content} IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :#{#command.content}, '%'))) " +
//      "AND (:#{#command.documentTypeSet} IS NULL OR t IN :#{#command.documentTypeSet})")
//  Page<DocumentPost> findDocumentPostsByFilter(@Param("command") DocumentCommand command, Pageable pageable);

  @Query(
      value = "SELECT DISTINCT p.* FROM document_post p " +
          "LEFT JOIN document_type dt ON p.document_post_id = dt.document_post_id " +
          "WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) " +
          "AND (:content IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))) " +
          "AND (:documentTypes IS NULL OR dt.document_type IN (:documentTypes)) " +
          "ORDER BY p.created_date DESC", // 동적 정렬은 여기서 기본값으로 설정
      countQuery = "SELECT COUNT(DISTINCT p.document_post_id) FROM document_post p " +
          "LEFT JOIN document_type dt ON p.document_post_id = dt.document_post_id " +
          "WHERE (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) " +
          "AND (:content IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))) " +
          "AND (:documentTypes IS NULL OR dt.document_type IN (:documentTypes))",
      nativeQuery = true
  )
  Page<DocumentPost> findDocumentPostsByFilter(
      String title,
      String subject,
      String content,
      Set<String> documentTypes,
      Pageable pageable
  );


  Optional<DocumentPost> findByDocumentPostId(UUID documentPostId);
}
