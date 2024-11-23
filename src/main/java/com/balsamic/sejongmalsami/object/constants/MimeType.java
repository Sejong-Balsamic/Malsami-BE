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
  // 이미지 MIME 타입
  JPEG("image/jpeg", UploadType.IMAGE),
  JPG("image/jpeg", UploadType.IMAGE),
  PNG("image/png", UploadType.IMAGE),
  GIF("image/gif", UploadType.IMAGE),
  BMP("image/bmp", UploadType.IMAGE),
  TIFF("image/tiff", UploadType.IMAGE),
  SVG("image/svg+xml", UploadType.IMAGE),
  WEBP("image/webp", UploadType.IMAGE),

  // 문서 MIME 타입
  PDF("application/pdf", UploadType.DOCUMENT),
  DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", UploadType.DOCUMENT),
  DOC("application/msword", UploadType.DOCUMENT),
  XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", UploadType.DOCUMENT),
  XLS("application/vnd.ms-excel", UploadType.DOCUMENT),
  PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", UploadType.DOCUMENT),
  PPT("application/vnd.ms-powerpoint", UploadType.DOCUMENT),
  HWP("application/x-hwp", UploadType.DOCUMENT),

  // 비디오 MIME 타입
  MP4("video/mp4", UploadType.VIDEO),
  AVI("video/x-msvideo", UploadType.VIDEO),
  MOV("video/quicktime", UploadType.VIDEO),

  // 음악 MIME 타입
  MP3("audio/mpeg", UploadType.MUSIC),
  WAV("audio/wav", UploadType.MUSIC),
  AAC("audio/aac", UploadType.MUSIC),
  OGG("audio/ogg", UploadType.MUSIC);

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

  // 특정 UploadType에 해당하는 MIME 타입 집합
  public static Set<String> getMimeTypesByUploadType(UploadType uploadType) {
    return Arrays.stream(MimeType.values())
        .filter(type -> type.getUploadType() == uploadType)
        .map(MimeType::getMimeType)
        .collect(Collectors.toSet());
  }

  // 각 UploadType별 유효성 검증 메서드
  public static boolean isValidImageMimeType(String mimeType) {
    return getMimeTypesByUploadType(UploadType.IMAGE).contains(mimeType.toLowerCase());
  }

  public static boolean isValidDocumentMimeType(String mimeType) {
    return getMimeTypesByUploadType(UploadType.DOCUMENT).contains(mimeType.toLowerCase());
  }

  public static boolean isValidVideoMimeType(String mimeType) {
    return getMimeTypesByUploadType(UploadType.VIDEO).contains(mimeType.toLowerCase());
  }

  public static boolean isValidMusicMimeType(String mimeType) {
    return getMimeTypesByUploadType(UploadType.MUSIC).contains(mimeType.toLowerCase());
  }
}