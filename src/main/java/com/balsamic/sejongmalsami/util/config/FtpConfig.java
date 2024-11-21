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

  // PROD

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

  // DEV

  @Value("${ftp.path-dev.document}")
  private String documentDevPath;

  @Value("${ftp.path-dev.question}")
  private String questionDevPath;

  @Value("${ftp.path-dev.answer}")
  private String answerDevPath;

  @Value("${ftp.path-dev.notice}")
  private String noticeDevPath;

  @Value("${ftp.path-dev.comment}")
  private String commentDevPath;

  @Value("${ftp.path-dev.document-request}")
  private String documentRequestDevPath;

  @Value("${ftp.path-dev.thumbnail}")
  private String thumbnailDevPath;

  @Value("${ftp.path-dev.courses}")
  private String coursesDevPath;

  // 썸네일 URL

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