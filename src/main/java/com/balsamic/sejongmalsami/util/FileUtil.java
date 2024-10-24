package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
   * 업로드 파일명 생성 (확장자 포함)
   */
  public static String generateUploadFileName(MultipartFile file) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = getBaseName(file.getOriginalFilename());
    String extension = getExtension(file.getOriginalFilename());
    return String.format("%s_%s.%s", curTimeStr, baseName, extension);
  }
}
