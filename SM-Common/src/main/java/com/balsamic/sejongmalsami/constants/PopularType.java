package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PopularType {
  DAILY("일간"),
  WEEKLY("주간");

  private final String description;
}
