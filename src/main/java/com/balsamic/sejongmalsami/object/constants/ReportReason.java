package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ReportReason {
  PROFANITY("비속어 및 모욕"),
  POLITICAL("정치적 발언"),
  SPAM("스팸 또는 도배"),
  COMMERCIAL_AD("광고 또는 판매"),
  INAPPROPRIATE_CONTENT("부적절한 내용"),
  OBSCENE_CONTENT("음란물"),
  LEAKAGE_IMPERSONATION("사기 또는 사칭"),
  OTHER("기타");

  private final String description;
}
