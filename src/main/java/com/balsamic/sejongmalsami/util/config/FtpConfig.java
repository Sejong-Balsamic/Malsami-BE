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

  @Value("${ftp.path.question}")
  private String questionPath;

  @Value("${ftp.path.answer}")
  private String answerPath;

  @Value("${ftp.path.notice}")
  private String noticePath;

  @Value("${ftp.path.comment}")
  private String commentPath;

  @Value("${ftp.path.document-request}")
  private String documentRequestPath;

  @Value("${ftp.path.thumbnail}")
  private String thumbnailPath;

  @Value("${ftp.path.courses}")
  private String coursesPath;

  @Value("${ftp.thumbnail-url.base-url}")
  private String thumbnailBaseUrl;

  @Value("${ftp.thumbnail-url.default-document}")
  private String defaultDocumentThumbnailUrl;

  @Value("${ftp.thumbnail-url.default-image}")
  private String defaultImageThumbnailUrl;

  @Value("${ftp.thumbnail-url.default-video}")
  private String defaultVideoThumbnailUrl;

  @Value("${ftp.thumbnail-url.default-music}")
  private String defaultMusicThumbnailUrl;
}