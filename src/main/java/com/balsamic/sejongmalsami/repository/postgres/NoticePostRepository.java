package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticePostRepository extends JpaRepository<NoticePost, UUID> {

  @Query("""
      select p from NoticePost p
      where (:query is null or p.title like %:query%)
      """)
  Page<NoticePost> findNoticePostsByFilter(
      @Param("query") String query,
      Pageable pageable
  );

  // 검색
  @Query(
      value = "SELECT DISTINCT p.* FROM sejong_malsami.public.notice_post p " +
              "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
              "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) ",
      countQuery = "SELECT COUNT(DISTINCT p.question_post_id) FROM question_post p " +
                   "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
                   "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) ",
      nativeQuery = true
  )
  Page<NoticePost> findNoticePostsByQuery(
      @Param("query") String query,
      Pageable pageable
  );
}
