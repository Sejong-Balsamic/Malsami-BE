package com.balsamic.sejongmalsami.config;

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

  // DEV FILE 업로드

  @Value("${ftp.path-file-dev.document}")
  private String documentFileDevPath;

  @Value("${ftp.path-file-dev.question}")
  private String questionFileDevPath;

  @Value("${ftp.path-file-dev.answer}")
  private String answerFileDevPath;

  @Value("${ftp.path-file-dev.notice}")
  private String noticeFileDevPath;

  @Value("${ftp.path-file-dev.comment}")
  private String commentFileDevPath;

  @Value("${ftp.path-file-dev.document-request}")
  private String documentRequestFileDevPath;

  @Value("${ftp.path-file-dev.thumbnail}")
  private String thumbnailFileDevPath;

  @Value("${ftp.path-file-dev.courses}")
  private String coursesFileDevPath;


  // 썸네일 URL

  @Value("${ftp.url.base-url}")
  private String baseUrl;

  @Value("${ftp.url.default-document}")
  private String defaultDocumentThumbnailUrl;

  @Value("${ftp.url.default-image}")
  private String defaultImageThumbnailUrl;

  @Value("${ftp.url.default-video}")
  private String defaultVideoThumbnailUrl;

  @Value("${ftp.url.default-music}")
  private String defaultMusicThumbnailUrl;

  // WEB 이미지 업로드

  @Value("${ftp.path-web-dev.document}")
  private String documentWebDevPath;

  @Value("${ftp.path-web-dev.question}")
  private String questionWebDevPath;

  @Value("${ftp.path-web-dev.answer}")
  private String answerWebDevPath;

  @Value("${ftp.path-web-dev.notice}")
  private String noticeWebDevPath;

  @Value("${ftp.path-web-dev.comment}")
  private String commentWebDevPath;

  @Value("${ftp.path-web-dev.document-request}")
  private String documentRequestWebDevPath;

  @Value("${ftp.path-web-dev.thumbnail}")
  private String thumbnailWebDevPath;

  @Value("${ftp.path-web-dev.courses}")
  private String coursesWebDevPath;



}