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
   * 시스템 운영체제 확인 (MAC, WINDOWS, LINUX, OTHER)
   * - WebP 이미지 지원 여부 체크용
   * @return SystemType 운영체제 타입
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
   * 파일명 생성
   * 1. ContentType 접두어 추가
   * 2. 특수문자/공백 제거
   * 3. UUID 추가
   * @return CONTENTTYPE_파일명_UUID.확장자
   */
  public static String generateFileName(ContentType contentType, String originalFilename) {
    String header = contentType.name();
    String baseName = sanitizeFileName(getBaseName(originalFilename));
    String extension = getExtension(originalFilename);
    String uuid = UUID.randomUUID().toString();
    return String.format("%s_%s_%s.%s", header, baseName, uuid, extension);
  }

  /**
   * 파일명 특수문자 제거 및 정제
   * 1. 특수문자/공백 -> 언더스코어
   * 2. 중복 언더스코어 제거
   * 3. 시작/끝 언더스코어 제거
   */
  private static String sanitizeFileName(String fileName) {
    return fileName.replaceAll("[\\[\\]\\{\\}\\(\\)\\s]+", "_")  // 특수문자와 공백을 언더스코어로
        .replaceAll("_{2,}", "_")                      // 중복 언더스코어 제거
        .replaceAll("^_|_$", "");                     // 시작과 끝의 언더스코어 제거
  }

  /**
   * 파일명에서 확장자를 제외한 기본 이름만 추출
   * @return 확장자 제외 파일명
   */
  public static String getBaseName(String filename) {
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1) {
      return filename;
    }
    return filename.substring(0, dotIndex);
  }

  /**
   * 파일명에서 확장자만 추출
   * @return 파일 확장자
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
   * MultipartFile 리스트를 ZIP으로 압축
   * - 파일이 없거나 1개인 경우 예외 발생
   * @return ZIP 파일 byte[]
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
   * 파일 경로에서 파일명만 추출
   * @return 파일명
   */
  public static String extractFileName(String filePath) {
    if (!StringUtils.hasText(filePath)) {
      throw new CustomException(ErrorCode.FILE_PATH_EMPTY);
    }

    int lastSeparatorIndex = filePath.lastIndexOf('/');
    if (lastSeparatorIndex == -1) {
      return filePath; // 경로에 '/'가 없는 경우 전체가 파일 이름으로 간주
    }

    return filePath.substring(lastSeparatorIndex + 1);
  }

  /**
   * 백분위 계산
   * @param total 전체 회원 수
   * @param value 특정 회원의 순위 (1위, 2위 등)
   * @return 백분위 (소숫점 두 자리까지)
   */
  public static Double calculatePercentile(int total, int value) {
    if (total == 0) {
      throw new CustomException(ErrorCode.PERCENTILE_CALCULATION_ERROR);
    }
    // 백분위 계산: 1등 → 1.00%, rank 2등 → 2.00% ... , rank 마지막등수 → 100.00%
    double rawPercentile = ((double) value / total) * 100;
    // 소숫점 두 자리까지 반올림
    double roundedPercentile = Math.round(rawPercentile * 100.0) / 100.0;
    return roundedPercentile;
  }
}
