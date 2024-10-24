package com.balsamic.sejongmalsami.util.config;

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

  @Value("${ftp.path.thumbnail-base-url}")
  private String thumbnailBaseUrl;

  @Value("${ftp.path.document-base-url}")
  private String documentBaseUrl;

  @Value("${ftp.basic.document}")
  private String defaultDocumentThumbnailUrl;

  @Value("${ftp.basic.image}")
  private String defaultImageThumbnailUrl;

  @Value("${ftp.basic.video}")
  private String defaultVideoThumbnailUrl;

  @Value("${ftp.basic.music}")
  private String defaultMusicThumbnailUrl;
}
