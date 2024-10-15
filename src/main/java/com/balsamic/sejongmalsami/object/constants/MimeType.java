package com.balsamic.sejongmalsami.object.constants;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MimeType {
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

  private static final Set<String> MIME_TYPES = Arrays
      .stream(MimeType.values())
      .map(MimeType::getMimeType)
      .collect(Collectors.toSet());

  // 유효한 MIME 타입인지 검증
  public static Boolean isValidMimeType(String mimeType) {
    return MIME_TYPES.contains(mimeType.toLowerCase());
  }

  // MIME 타입을 Enum으로 변환
  public static MimeType fromString(String mimeType) {
    return Arrays.stream(MimeType.values())
        .filter(type -> type.getMimeType().equalsIgnoreCase(mimeType))
        .findFirst()
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_FILE_FORMAT));
  }
}
