package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.postgres.AnswerPost;
import com.balsamic.sejongmalsami.postgres.QuestionPost;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerPostRepository extends JpaRepository<AnswerPost, UUID> {

  // 특정 질문글에 작성된 답변 수 조회
  Integer countByQuestionPost(QuestionPost questionPost);

  // 특정 질문글에 작성된 모든 답변 List 조회
  Optional<List<AnswerPost>> findAllByQuestionPost(QuestionPost questionPost);

  // 특정 질문글에 작성된 답변 조회 시 채택 된 글이 최상단에 위치
  Optional<List<AnswerPost>> findAllByQuestionPostOrderByIsChaetaekDescCreatedDateDesc(QuestionPost questionPost);

  List<AnswerPost> findByMember(Member member);

  long countByMember(Member member);
}
