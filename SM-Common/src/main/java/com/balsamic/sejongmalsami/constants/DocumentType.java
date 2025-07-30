package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
  DOCUMENT("자료: 필기 자료, 교안, 녹화본, 실험/실습 자료"),
  PAST_EXAM("기출: 퀴즈, 기출 문제, 과제"),
  SOLUTION("해설: 솔루션");
  
  private final String description;
}