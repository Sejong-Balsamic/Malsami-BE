package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileStatus {
  EMPTY("비어 있음"),
  MOCK_DATA("목 데이터값"),
  NOT_FOUND("찾을 수 없음"),
  INVALID("유효하지 않음"),
  DEPRECATED("더 이상 사용되지 않음"),
  INITIAL("초기 상태"),

  PENDING("대기중"),
  IN_PROGRESS("진행중"),
  SUCCESS("성공"),
  FAILURE("실패");

  private final String description;
}
