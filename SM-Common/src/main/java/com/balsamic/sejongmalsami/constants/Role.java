package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
  ROLE_USER("일반 회원"),
  ROLE_ADMIN("관리자");

  private final String description;
}
