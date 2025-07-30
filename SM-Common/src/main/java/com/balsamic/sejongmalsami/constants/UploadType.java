package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadType {
  IMAGE("이미지 파일"),
  DOCUMENT("문서 파일"),
  VIDEO("비디오 파일"),
  MUSIC("음원 파일");

  private final String description;
}