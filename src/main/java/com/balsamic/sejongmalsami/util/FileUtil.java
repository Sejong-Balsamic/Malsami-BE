package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.SystemType;
import org.springframework.util.StringUtils;

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
   * @throws IllegalArgumentException 파일 이름이 null이거나 비어있는 경우
   */
  public static String getBaseName(String filename) {
    if (!StringUtils.hasText(filename)) {
      throw new IllegalArgumentException("파일 이름은 null이거나 비어있을 수 없습니다.");
    }

    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex == -1) {
      return filename;
    }
    return filename.substring(0, dotIndex);
  }

  /**
   * 파일 이름에서 확장자를 추출하여 반환합니다.
   *
   * @param filename 확장자가 포함된 파일 이름
   * @return 파일의 확장자 (없을 경우 빈 문자열)
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


}
