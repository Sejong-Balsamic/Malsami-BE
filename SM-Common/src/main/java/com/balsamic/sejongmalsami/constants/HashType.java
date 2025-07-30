package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HashType {
  DEPARTMENT_JSON("학과 정보 파일 해시값"),
  COURSE_FILES("복수의 교과목명 파일 통합 해시값"),
  SERVER_ERROR_CODES("스프링 서버 에러코드 해시값");

  private final String description;
}
