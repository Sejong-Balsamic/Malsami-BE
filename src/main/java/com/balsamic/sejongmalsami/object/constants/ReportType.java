package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportType {
  QUESTION_POST("질문 게시글"),
  ANSWER_POST("답변 게시글"),
  DOCUMENT_BOARD("자료 게시글"),
  DOCUMENT_REQUEST_POST("자료요청 게시글");

  private final String description;
}
