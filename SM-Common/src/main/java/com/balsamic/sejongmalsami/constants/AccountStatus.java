package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountStatus {
  ACTIVE("활성화된 계정"),
  DELETED("삭제된 계정");

  private final String description;
}
