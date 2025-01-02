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
}
