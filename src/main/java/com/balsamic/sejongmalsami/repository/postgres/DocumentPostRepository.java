package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.DocumentPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentPostRepository extends JpaRepository<DocumentPost, UUID> {

  // 일간 인기글 계산 (글 작성일이 startDate 보다 나중인 질문 게시글)
  @Query("SELECT p FROM DocumentPost p WHERE p.createdDate >= :startDate")
  List<DocumentPost> findDocumentPostsAfter(LocalDateTime startDate);

  // 일간 인기글 상위 30개 조회
  List<DocumentPost> findTop30ByOrderByDailyScoreDesc();

  // 주간 인기글 상위 30개 조회
  List<DocumentPost> findTop30ByOrderByWeeklyScoreDesc();
}
