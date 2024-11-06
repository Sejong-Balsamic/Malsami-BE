package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortType {
  LATEST("최신 순"),
  MOST_LIKED("좋아요 순"),
  YEOPJEON_REWARD("엽전 현상금 순"),
  VIEW_COUNT("조회수 순");

  private final String description;
}
