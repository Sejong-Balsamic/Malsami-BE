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
  // 이미지
  JPEG("image/jpeg"),
  JPG("image/jpeg"),
  PNG("image/png"),
  GIF("image/gif"),
  BMP("image/bmp"),
  TIFF("image/tiff"),
  SVG("image/svg+xml"),
  WEBP("image/webp"),
  // 동영상
  MP4("video/mp4"),
  AVI("video/x-msvideo"),
  MOV("video/quicktime"),
  // 음악
  MP3("audio/mpeg"),
  WAV("audio/wav"),
  AAC("audio/aac"),
  // 문서
  DOC("application/msword"),
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  XLS("application/vnd.ms-excel"),
  XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
  PPT("application/vnd.ms-powerpoint"),
  PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
  OGG("application/ogg"),
  PDF("application/pdf");

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
