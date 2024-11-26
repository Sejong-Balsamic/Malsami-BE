package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SortType {
  LATEST("최신순"),
  OLDEST("과거순"),
  MOST_LIKED("추천순"),
  YEOPJEON_REWARD("엽전 현상금 높은순"),
  VIEW_COUNT("조회수 많은순"),
  COMMENT_COUNT("댓글순"),
  DOWNLOAD_COUNT("다운로드순");

  private final String description;
}
