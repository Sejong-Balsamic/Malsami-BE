package com.balsamic.sejongmalsami.util.config;

import java.nio.file.Path;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FtpConfig {
  @Value("${ftp.server}")
  private String server;

  @Value("${ftp.port}")
  private Integer port;

  @Value("${ftp.user}")
  private String user;

  @Value("${ftp.pass}")
  private String pass;

  @Value("${ftp.path.document}")
  private String documentPath;

  @Value("${ftp.path.thumbnail}")
  private String thumbnailPath;
}
