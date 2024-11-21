package com.balsamic.sejongmalsami.util.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * FTP 서버 설정 클래스
 */
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

  @Value("${ftp.path.media}")
  private String mediaPath;

  @Value("${ftp.thumbnail-base-url}")
  private String thumbnailBaseUrl;

  @Value("${ftp.document-base-url}")
  private String documentBaseUrl;

  @Value("${ftp.media-base-url}")
  private String mediaBaseUrl;

  @Value("${ftp.basic.document}")
  private String defaultDocumentThumbnailUrl;

  @Value("${ftp.basic.image}")
  private String defaultImageThumbnailUrl;

  @Value("${ftp.basic.video}")
  private String defaultVideoThumbnailUrl;

  @Value("${ftp.basic.music}")
  private String defaultMusicThumbnailUrl;
}
