package com.balsamic.sejongmalsami.post.repository.postgres;

import com.balsamic.sejongmalsami.constants.DocumentType;
import com.balsamic.sejongmalsami.constants.PostTier;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentPostRepository extends JpaRepository<DocumentPost, UUID> {

  // 전체 자료 글 dailyScore 초기화
  @Modifying
  @Query("UPDATE DocumentPost p SET p.dailyScore = 0")
  void resetDailyScore();

  // 전체 자료 글 weeklyScore 초기화
  @Modifying
  @Query("UPDATE DocumentPost p SET p.weeklyScore = 0")
  void resetWeeklyScore();

  // 자료 글 교과목명, 태그 필터링
  @Query("SELECT DISTINCT p FROM DocumentPost p " +
      "LEFT JOIN p.documentTypes dt " +
      "WHERE " +
      "(:subject IS NULL OR p.subject = :subject) " +
      "AND (:documentTypes IS NULL OR dt IN :documentTypes) " +
      "AND (:faculty IS NULL OR :faculty member of p.faculties) " +
      "AND (:postTier IS NULL OR p.postTier = :postTier)")
  Page<DocumentPost> findDocumentPostsByFilter(
      @Param("subject") String subject,
      @Param("documentTypes") List<DocumentType> documentTypes,
      @Param("faculty") String faculty,
      @Param("postTier") PostTier postTier,
      Pageable pageable
  );

  // Hot 다운로드 (일단 전체 다운로드 수 기준으로 찾습니다)
  @Query("""
        select dp from DocumentPost dp
        left join DocumentFile df on df.documentPost = dp
        group by dp
        order by max (df.totalDownloadCount) desc
      """)
  Page<DocumentPost> findHotDownloads(Pageable pageable);

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

  // 내가 작성한 자료글
  Page<DocumentPost> findAllByMember(Member member, Pageable pageable);

  long countByMember(Member member);

  long countByMemberAndIsPopularTrue(Member member);
}
