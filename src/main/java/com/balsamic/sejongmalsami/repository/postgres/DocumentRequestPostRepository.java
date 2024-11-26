package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
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
      @Param("faculty") Faculty faculty,
      @Param("documentTypes") List<DocumentType> documentTypes,
      Pageable pageable);

  // 검색
  @Query(
      value = """
          select distinct p.* from document_request_post p 
          where (:query) is null or lower(p.title) like lower(concat('%', :query, '%'))
          and (:query) is null or lower(p.content) like lower(concat('%', :query, '%'))
          and (:subject) is null or lower(p.subject) like lower(concat('%', :subject, '%'))
          order by p.created_date desc 
          fetch first ? rows only 
          """,
      countQuery = """
          select count (distinct p.document_request_post_id) from document_request_post p
          where (:query is null or lower(p.title) like lower(concat('%', :query, '%')))
          and (:query) is null or lower(p.content) like lower(concat('%', :query, '%'))
          and (:subject) is null or lower (p.subject) like lower(concat('%', :subject, '%'))
          """,
      nativeQuery = true
  )
  Page<DocumentRequestPost> findDocumentRequestPostsByQuery(
      @Param("query") String query,
      @Param("subject") String subject,
      Pageable pageable
  );
}
