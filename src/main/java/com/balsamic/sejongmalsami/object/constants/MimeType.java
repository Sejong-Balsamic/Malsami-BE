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
  JPEG("image/jpeg", UploadType.IMAGE),
  JPG("image/jpeg", UploadType.IMAGE),
  PNG("image/png", UploadType.IMAGE),
  GIF("image/gif", UploadType.IMAGE),
  BMP("image/bmp", UploadType.IMAGE),
  TIFF("image/tiff", UploadType.IMAGE),
  SVG("image/svg+xml", UploadType.IMAGE),
  WEBP("image/webp", UploadType.IMAGE),
  // 동영상
  MP4("video/mp4", UploadType.VIDEO),
  AVI("video/x-msvideo", UploadType.VIDEO),
  MOV("video/quicktime", UploadType.VIDEO),
  // 음악
  MP3("audio/mpeg", UploadType.MUSIC),
  WAV("audio/wav", UploadType.MUSIC),
  AAC("audio/aac", UploadType.MUSIC),
  OGG("audio/ogg", UploadType.MUSIC),
  // 문서
  DOC("application/msword", UploadType.DOCUMENT),
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", UploadType.DOCUMENT),
  XLS("application/vnd.ms-excel", UploadType.DOCUMENT),
  XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", UploadType.DOCUMENT),
  PPT("application/vnd.ms-powerpoint", UploadType.DOCUMENT),
  PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", UploadType.DOCUMENT),
  PDF("application/pdf", UploadType.DOCUMENT);

  private final String mimeType;
  private final UploadType uploadType;

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
