package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReactionType {
  LIKE("좋아요"),
  DISLIKE("싫어요");

  private final String description;
}