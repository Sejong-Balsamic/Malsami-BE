package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LikeType {
  LIKE("좋아요"),
  DISLIKE("싫어요");

  private final String description;
}