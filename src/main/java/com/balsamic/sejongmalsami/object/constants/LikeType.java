package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeType {
  QUESTION_POST("질문 게시글"),
  ANSWER_POST("답변 게시글"),
  NOTICE("공지사항 게시글"),
  DOCUMENT_REQUEST_POST("자료 요청 게시글"),
  DOCUMENT("자료 게시글"),
  COMMENT("댓글");
  private final String description;
}
