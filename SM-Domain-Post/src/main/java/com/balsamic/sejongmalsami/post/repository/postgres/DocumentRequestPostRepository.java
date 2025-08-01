package com.balsamic.sejongmalsami.post.repository.postgres;

import com.balsamic.sejongmalsami.constants.DocumentType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentRequestPost;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRequestPostRepository extends JpaRepository<DocumentRequestPost, UUID> {

  // 자료 요청글 필터링
  @Query("""
    select distinct d
    from DocumentRequestPost d
    left join d.documentTypes dt
    where
    (:subject is null or d.subject = :subject)
    and (:faculty is null or :faculty member of d.faculties)
    and (:documentTypes is null or dt in :documentTypes)
    """)
  Page<DocumentRequestPost> findFilteredDocumentRequestPost(
      @Param("subject") String subject,
      @Param("faculty") String faculty,
      @Param("documentTypes") List<DocumentType> documentTypes,
      Pageable pageable);

  // 검색
  @Query(
      value = "SELECT DISTINCT p.* FROM document_request_post p " +
          "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
          "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) ",
      countQuery = "SELECT COUNT(DISTINCT p.document_request_post_id) FROM document_request_post p " +
          "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
          "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
          "AND (:subject IS NULL OR LOWER(p.subject) LIKE LOWER(CONCAT('%', :subject, '%'))) ",
      nativeQuery = true
  )
  Page<DocumentRequestPost> findDocumentRequestPostsByQuery(
      @Param("query") String query,
      @Param("subject") String subject,
      Pageable pageable
  );

  List<DocumentRequestPost> findByMember(Member member);

  // 내가 작성한 자료요청글
  Page<DocumentRequestPost> findAllByMember(Member member, Pageable pageable);

  long countByMember(Member member);
}
