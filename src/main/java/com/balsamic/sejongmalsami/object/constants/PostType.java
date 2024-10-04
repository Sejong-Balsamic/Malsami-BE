package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PostType {
  QUESTION("질문글"),
  ANSWER("답변글"),
  DOCUMENT("자료글"),
  DOCUMENT_REQUEST("자료요청글");

  private final String description;
}
