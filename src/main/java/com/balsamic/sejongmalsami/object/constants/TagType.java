package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TagType {
  PRESET("정적 태그"),
  CUSTOM("커스텀 태그");

  private final String description;
}
