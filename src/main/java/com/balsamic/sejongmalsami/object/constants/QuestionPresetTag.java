package com.balsamic.sejongmalsami.object.constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "질문 게시글의 정적 태그")
public enum QuestionPresetTag {

  OUT_OF_CLASS("수업 외 내용"),

  UNKNOWN_CONCEPT("개념 모름"),

  BETTER_SOLUTION("더 나은 풀이"),

  EXAM_PREPARATION("시험 대비"),

  DOCUMENT_REQUEST("자료 요청"),

  STUDY_TIPS("공부 팁"),

  ADVICE_REQUEST("조언 구함");

  private final String description; // 태그 한글 이름
}
