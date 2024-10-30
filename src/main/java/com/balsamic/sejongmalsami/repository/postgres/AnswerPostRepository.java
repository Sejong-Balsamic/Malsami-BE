package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerPostRepository extends JpaRepository<AnswerPost, UUID> {

  // 특정 질문글에 작성된 모든 답변 List 조회
  List<AnswerPost> findAnswerPostsByQuestionPost(QuestionPost questionPost);

  // 특정 질문글에 작성된 답변 수 조회
  Integer countByQuestionPost(QuestionPost questionPost);
}
