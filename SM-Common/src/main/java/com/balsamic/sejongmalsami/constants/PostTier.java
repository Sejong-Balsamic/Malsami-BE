package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostTier {
  CHEONMIN("천민"),
  JUNGIN("중인"),
  YANGBAN("양반"),
  KING("왕");

  private final String description;
}
