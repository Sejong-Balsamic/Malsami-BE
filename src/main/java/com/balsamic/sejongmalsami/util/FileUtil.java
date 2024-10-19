package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.SystemType;

public class FileUtil {

  /**
   * 현재 운영체제 반환
   * @return
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
}
