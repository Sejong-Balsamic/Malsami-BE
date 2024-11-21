package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * FTP 스토리지 서비스 구현 클래스
 */
@Service("ftpStorageService")
@Slf4j
@RequiredArgsConstructor
public class FtpStorageService implements StorageService {

  private final GenericObjectPool<FTPClient> ftpClientPool;
  private final FtpConfig ftpConfig;

  @Override
  public String uploadFile(ContentType contentType, MultipartFile file) {
    String uploadFileName = FileUtil.generateFileName(contentType, file.getOriginalFilename());
    String remoteFilePath = ftpConfig.getMediaPath() + "/" + uploadFileName;

    log.info("FTP 파일 업로드 시작: {} -> {}", file.getOriginalFilename(), remoteFilePath);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = file.getInputStream()) {
        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (success) {
          log.info("FTP 파일 업로드 성공: {}", remoteFilePath);
          return ftpConfig.getMediaBaseUrl() + uploadFileName;
        } else {
          log.error("FTP 파일 업로드 실패: {}", remoteFilePath);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (Exception e) {
      log.error("FTP 파일 업로드 중 예외 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
        log.debug("FTPClient 반환 완료: {}", ftpClient);
      }
    }
  }

  @Override
  public String uploadThumbnail(ContentType contentType, MultipartFile file) {
    String uploadFileName = FileUtil.generateFileName(contentType, file.getOriginalFilename());

    // 썸네일 업로드시 접근 URL 생성
    String remoteFilePath = ftpConfig.getThumbnailPath() + "/" + uploadFileName;

    log.info("FTP 썸네일 업로드 시작: {} -> {}", file.getOriginalFilename(), remoteFilePath);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = file.getInputStream()) {
        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (success) {
          log.info("FTP 썸네일 업로드 성공: {}", remoteFilePath);
          return ftpConfig.getThumbnailBaseUrl() + uploadFileName;
        } else {
          log.error("FTP 썸네일 업로드 실패: {}", remoteFilePath);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (Exception e) {
      log.error("FTP 썸네일 업로드 중 예외 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
        log.debug("FTPClient 반환 완료: {}", ftpClient);
      }
    }
  }

  @Override
  public void deleteFile(ContentType contentType, String fileUrl) {
    String fileName = extractFileName(fileUrl);
    String remoteFilePath =
        switch (contentType) {
          case THUMBNAIL -> ftpConfig.getThumbnailPath() + "/" + fileName;
          case DOCUMENT -> ftpConfig.getDocumentPath() + "/" + fileName;
          default -> ftpConfig.getMediaPath() + "/" + fileName;
        };

    log.info("FTP 파일 삭제 시작: {}", remoteFilePath);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      boolean deleted = ftpClient.deleteFile(remoteFilePath);
      if (deleted) {
        log.info("FTP 파일 삭제 성공: {}", remoteFilePath);
      } else {
        log.warn("FTP 파일 삭제 실패: {}", remoteFilePath);
        throw new CustomException(ErrorCode.FTP_FILE_DELETE_ERROR);
      }
    } catch (Exception e) {
      log.error("FTP 파일 삭제 중 예외 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_DELETE_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
        log.debug("FTPClient 반환 완료: {}", ftpClient);
      }
    }
  }

  /**
   * FTP URL에서 파일명을 추출
   */
  private String extractFileName(String fileUrl) {
    return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
  }
}
