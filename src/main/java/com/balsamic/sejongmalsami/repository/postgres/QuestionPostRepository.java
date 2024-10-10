package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.QuestionPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QuestionPostRepository extends JpaRepository<QuestionPost, UUID> {

  // 글 작성일이 startDate 보다 나중인 질문 게시글
  @Query("SELECT p FROM QuestionPost p WHERE p.createdDate >= :startDate")
  List<QuestionPost> findQuestionPostsAfter(LocalDateTime startDate);
}