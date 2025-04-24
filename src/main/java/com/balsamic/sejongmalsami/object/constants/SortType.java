package com.balsamic.sejongmalsami.object.constants;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;

@AllArgsConstructor
@Getter
public enum SortType {
  LATEST("최신순"),
  OLDEST("과거순"),
  MOST_LIKED("추천순"),
  REWARD_YEOPJEON_DESCENDING("엽전 현상금 높은순"),
  REWARD_YEOPJEON_LATEST("엽전 현상금 존재 최신순"),
  VIEW_COUNT("조회수 많은순"),
  COMMENT_COUNT("댓글순"),
  DOWNLOAD_COUNT("다운로드순");

  private final String description;

  // JPQL 사용 시 Sort 반환 메서드
  public static Sort getJpqlSortOrder(SortType sortType) {
    Sort sort;
    switch (sortType) {
      case LATEST -> sort =  Sort.by(Sort.Order.desc("createdDate"));
      case OLDEST -> sort = Sort.by(Sort.Order.asc("createdDate"));
      case MOST_LIKED -> sort = Sort.by(Sort.Order.desc("likeCount"));
      case REWARD_YEOPJEON_DESCENDING -> sort = Sort.by(Sort.Order.desc("rewardYeopjeon"));
      case VIEW_COUNT -> sort = Sort.by(Sort.Order.desc("viewCount"));
      case COMMENT_COUNT -> sort = Sort.by(Sort.Order.desc("commentCount"));
      case DOWNLOAD_COUNT -> sort = Sort.by(Sort.Order.desc("downloadCount"));
      default -> throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
    }
    return sort;
  }

  // 네이티브 쿼리 사용 시 Sort 반환 메서드
  public static Sort getNativeQuerySortOrder(SortType sortType) {
    Sort sort;
    switch (sortType) {
      case LATEST -> sort =  Sort.by(Sort.Order.desc("created_date"));
      case OLDEST -> sort = Sort.by(Sort.Order.asc("created_date"));
      case MOST_LIKED -> sort = Sort.by(Sort.Order.desc("like_count"));
      case REWARD_YEOPJEON_DESCENDING -> sort = Sort.by(Sort.Order.desc("reward_yeopjeon"));
      case VIEW_COUNT -> sort = Sort.by(Sort.Order.desc("view_count"));
      case COMMENT_COUNT -> sort = Sort.by(Sort.Order.desc("comment_count"));
      case DOWNLOAD_COUNT -> sort = Sort.by(Sort.Order.desc("download_count"));
      default -> throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
    }
    return sort;
  }
}
