package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActionType {
  // TODO: 경험치 변동에 대한 정의 필요 (수정, 공유, 삭제...)
  LIKE("좋아요"),
  POST("게시글 작성"),
  COMMENT("댓글 작성"),
  SHARE("공유"),
  UPDATE("수정"),
  DELETE("삭제");

  private final String description;
}
