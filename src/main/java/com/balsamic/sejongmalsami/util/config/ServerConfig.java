package com.balsamic.sejongmalsami.util.config;

import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.util.FileUtil;
import java.nio.file.Path;

public class ServerConfig {
  public static final SystemType currentSystemType = FileUtil.getCurrentSystem();

  public static final Boolean isLinuxServer = FileUtil.getCurrentSystem().equals(SystemType.LINUX);

  public static Path coursePath;

  public static Path departmentPath;
}
