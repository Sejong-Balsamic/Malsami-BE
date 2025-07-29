package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationCategory {
  COMMON("일반 알림", "새로운 알림이 있습니다.", "{body}"),
  NOTICE("공지 알림", "새로운 공지사항이 등록되었습니다.", "새로운 공지가 등록되었습니다: {title}"),
  LIKE("좋아요 증가", "작성한 글에 좋아요가 증가했습니다", "{title} 글의 좋아요가 증가했습니다."),
  POPULAR_POST("인기글 등록", "작성한 글이 인기글에 올라갔습니다.", "{title} 글이 인기글에 올라갔습니다.");

  private final String description;
  private final String defaultTitle;
  private final String defaultBody;
}
