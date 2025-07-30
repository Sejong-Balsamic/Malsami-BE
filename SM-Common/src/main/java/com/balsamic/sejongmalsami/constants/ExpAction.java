package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExpAction {

  CREATE_QUESTION_POST("질문 글 작성"),
  CREATE_DOCUMENT_POST("자료 글 작성"),
  CREATE_ANSWER_POST("답변 글 작성"),
  CREATE_COMMENT("댓글 작성"),
  PURCHASE_DOCUMENT("자료 구매"),
  CHAETAEK_CHOSEN("답변 채택됨"),
  CHAETAEK_ACCEPT("답변 채택함"),
  RECEIVE_LIKE("좋아요 받음");


  private final String description;
}
