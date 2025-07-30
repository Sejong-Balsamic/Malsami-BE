package com.balsamic.sejongmalsami.dto;

import com.balsamic.sejongmalsami.constants.SystemType;
import com.balsamic.sejongmalsami.util.FileUtil;
import java.nio.file.Path;

public class ServerInfo {
  public static final SystemType currentSystemType = FileUtil.getCurrentSystem();

  public static final Boolean isLinuxServer = FileUtil.getCurrentSystem().equals(SystemType.LINUX);

  public static Path coursePath;

  public static Path departmentPath;
}
