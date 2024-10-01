package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DocumentType {
  DOCUMENT("자료"),
  PAST("기출"),
  SOLUTION("해설");


  private final String description;
}
