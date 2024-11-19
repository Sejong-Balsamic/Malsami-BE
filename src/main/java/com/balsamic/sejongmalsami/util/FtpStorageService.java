package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("ftpStorageService")
@Primary // 우선적으로 ftpStorageService 사용
@Slf4j
@RequiredArgsConstructor
public class FtpStorageService implements StorageService {

  private final GenericObjectPool<FTPClient> ftpClientPool;
  private final FtpConfig ftpConfig;

  @Override
  public String uploadFile(String contentType, MultipartFile file) {
    String uploadFileName = FileUtil.generateFileName(contentType, file.getOriginalFilename());
    String remoteFile = ftpConfig.getDocumentPath() + "/" + uploadFileName;
    log.info("FTP 파일 업로드 시작: {} -> {}", file.getOriginalFilename(), remoteFile);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = file.getInputStream()) {
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        if (success) {
          log.info("FTP 파일 업로드 성공: {}", remoteFile);
          return uploadFileName;
        } else {
          log.error("FTP 파일 업로드 실패: {}", remoteFile);
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
  public void deleteFile(String fileUrl) {
    String remoteFilePath = ftpConfig.getDocumentPath() + "/" + extractFileName(fileUrl);
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
