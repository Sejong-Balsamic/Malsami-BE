package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 관련 유틸리티 클래스
 */

@Slf4j
public class FileUtil {
  /**
   * 현재 운영체제 반환
   */
  public static SystemType getCurrentSystem() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.contains("win")) {
      return SystemType.WINDOWS;
    } else if (osName.contains("mac")) {
      return SystemType.MAC;
    } else if (osName.contains("nux") || osName.contains("nix")) {
      return SystemType.LINUX;
    } else {
      return SystemType.OTHER;
    }
  }

  /**
   * 파일명 생성 (UUID 사용, ContentType 제거)
   *
   * @param originalFilename 원본 파일명
   * @return 생성된 파일명
   */
  public static String generateFileName(ContentType contentType, String originalFilename) {
    String header = contentType.name();
    String baseName = sanitizeFileName(getBaseName(originalFilename));
    String extension = getExtension(originalFilename);
    String uuid = UUID.randomUUID().toString();
    return String.format("%s_%s_%s.%s", header, baseName, uuid, extension);
  }

  // 파일이름에 특수문자 제거
  private static String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[\\[\\]\\{\\}\\(\\)\\s]+", "_")  // 특수문자와 공백을 언더스코어로
        .replaceAll("_{2,}", "_")                      // 중복 언더스코어 제거
        .replaceAll("^_|_$", "");                     // 시작과 끝의 언더스코어 제거
  }

  /**
   * 파일 이름에서 확장자를 제거한 기본 이름을 반환합니다.
   *
   * @param filename 확장자가 포함된 파일 이름
   * @return 확장자가 제거된 파일 기본 이름
   */
  public static String getBaseName(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1) {
      return filename;
    }
    return filename.substring(0, dotIndex);
  }

  /**
   * 파일 이름에서 확장자를 추출하여 반환합니다.
   */
  public static String getExtension(String filename) {
    if (!StringUtils.hasText(filename)) {
      return "";
    }

    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1 || dotIndex == filename.length() - 1) {
      return "";
    }
    return filename.substring(dotIndex + 1);
  }

  /**
   * 여러 MultipartFile을 ZIP 파일로 압축하여 byte 배열 반환
   */
  public static byte[] zipFiles(List<MultipartFile> files) throws IOException {
    // files 유효성 검사
    if (files == null || files.isEmpty() || files.size() == 1) {
      throw new CustomException(ErrorCode.EMPTY_OR_SINGLE_FILE_FOR_ZIP);
    }

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (MultipartFile file : files) {
        ZipEntry entry = new ZipEntry(file.getOriginalFilename());
        zos.putNextEntry(entry);
        zos.write(file.getBytes());
        zos.closeEntry();
        log.debug("ZIP에 파일 추가: {}", file.getOriginalFilename());
      }
    }
    return baos.toByteArray();
  }

  /**
   * 주어진 경로에서 파일 이름(확장자 포함)을 추출합니다.
   *
   * @param filePath 파일 경로
   * @return 파일 이름 반환
   */
  public static String extractFileName(String filePath) {
    if (!StringUtils.hasText(filePath)) {
      throw new IllegalArgumentException("파일 경로가 비어 있거나 null입니다.");
    }

    int lastSeparatorIndex = filePath.lastIndexOf('/');
    if (lastSeparatorIndex == -1) {
      return filePath; // 경로에 '/'가 없는 경우 전체가 파일 이름으로 간주
    }

    return filePath.substring(lastSeparatorIndex + 1);
  }
}
