package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Grade {
  COMMONER("천민"),
  MIDDLE_CLASS("중인"),
  NOBLE("양반"),
  KING("왕");

  private final String description;
}
