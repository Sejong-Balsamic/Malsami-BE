package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionType {
  LIKE("좋아요"),
  POST("게시글 작성"), // TODO: 질문글, 답변글, 자료글 작성 세분화?
  COMMENT("댓글 작성"),
  UPDATE("수정"),
  DELETE("삭제");

  private final String description;
}
