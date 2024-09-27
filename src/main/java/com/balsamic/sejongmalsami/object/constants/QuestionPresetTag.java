package com.balsamic.sejongmalsami.object.constants;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "질문 게시글의 정적 태그")
public enum QuestionPresetTag {
  @Schema(description = "수업 외 내용")
  OUT_OF_CLASS(0, "수업 외 내용"),

  @Schema(description = "개념 모름")
  UNKNOWN_CONCEPT(1, "개념 모름"),

  @Schema(description = "더 나은 풀이")
  BETTER_SOLUTION(2, "더 나은 풀이"),

  @Schema(description = "시험 대비")
  EXAM_PREPARATION(3, "시험 대비"),

  @Schema(description = "자료 요청")
  DOCUMENT_REQUEST(4, "자료 요청"),

  @Schema(description = "공부 팁")
  STUDY_TIPS(5, "공부 팁"),

  @Schema(description = "조언 구함")
  ADVICE_REQUEST(6, "조언 구함");

  private final Integer code; // 숫자 코드
  private final String description; // 태그 한글 이름

  @JsonValue
  public String getDescription() {
    return description;
  }

  // 숫자 코드로 Enum을 가져오는 메서드
  public static QuestionPresetTag fromCode(int code) {
    for (QuestionPresetTag tag : QuestionPresetTag.values()) {
      if (tag.code.equals(code)) {
        return tag;
      }
    }
    throw new CustomException(ErrorCode.QUESTION_PRESET_TAG_CODE_INVALID);
  }
}
