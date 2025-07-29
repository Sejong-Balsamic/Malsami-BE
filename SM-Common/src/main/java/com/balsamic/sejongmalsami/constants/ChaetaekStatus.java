package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChaetaekStatus {
  ALL("전체"),
  CHAETAEK("채택"),
  NO_CHAETAEK("미채택");

  private final String description;
}
