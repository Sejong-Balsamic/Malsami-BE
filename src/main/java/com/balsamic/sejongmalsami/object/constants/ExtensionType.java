package com.balsamic.sejongmalsami.object.constants;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ExtensionType {
  JPEG("image/jpeg"),
  JPG("image/jpeg"),
  PNG("image/png"),
  MP4("video/mp4"),
  ZIP("application/zip"),
  AVI("video/x-msvideo"),
  MOV("video/quicktime"),
  MP3("audio/mpeg"),
  WAV("audio/wav"),
  AAC("audio/aac");

  private final String mimeType;

  // 유효한 MIME 타입인지 검증
  public static Boolean isValidMimeType(String mimeType) {
    return Arrays.stream(ExtensionType.values())
        .map(ExtensionType::getMimeType)
        .toList()
        .contains(mimeType.toLowerCase());
  }
}
