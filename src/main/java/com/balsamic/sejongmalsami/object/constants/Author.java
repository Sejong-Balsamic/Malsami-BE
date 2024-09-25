package com.balsamic.sejongmalsami.object.constants;

// Swagger DOCS에서 @ApiChangeLog 어노테이션에 사용
public enum Author {
  SUHSAECHAN("서새찬"),
  BAEKMINHONG("백민홍"),
  BAEKJIHUN("백지훈");

  private final String displayName;

  Author(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
