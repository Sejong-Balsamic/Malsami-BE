package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UploadType {
  IMAGE("이미지 파일"),
  MEDIA("미디어 파일: 영상, 음원"),
  DOCUMENT("문서 파일");

  private final String description;
}