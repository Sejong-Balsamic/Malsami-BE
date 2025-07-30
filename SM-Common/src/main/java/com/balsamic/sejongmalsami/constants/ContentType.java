package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ContentType {
  THUMBNAIL("썸네일"),
  COMMENT("댓글"),
  QUESTION("질문글"),
  ANSWER("답변글"),
  NOTICE("공지사항"),
  DOCUMENT("자료글"),
  COURSES("교과목"),
  DOCUMENT_REQUEST("자료요청글");

  private final String description;
}