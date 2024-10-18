package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class FtpUtil {

  private final GenericObjectPool<FTPClient> ftpClientPool;
  private final FtpConfig ftpConfig;

  /**
   * FTP를 통해 단일 파일 업로드 (비동기)
   *
   * @param multipartFile 업로드할 멀티파트 파일 객체
   */
  @Async
  public void uploadFileAsync(MultipartFile multipartFile) {
    uploadFile(multipartFile);
  }

  /**
   * FTP를 통해 단일 파일 업로드
   *
   * @param multipartFile 업로드할 멀티파트 파일 객체
   */
  public void uploadFile(MultipartFile multipartFile) {
    String remoteFile = ftpConfig.getDocumentPath() + "/" + multipartFile.getOriginalFilename();
    log.info("FTP 파일 업로드 시작: {} -> {}", multipartFile.getOriginalFilename(), remoteFile);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = multipartFile.getInputStream()) {
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        if (success) {
          log.info("FTP 파일 업로드 성공: {}", remoteFile);
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
      }
    }
  }

  /**
   * FTP를 통해 여러 파일 업로드 (비동기)
   *
   * @param multipartFiles 업로드할 멀티파트 파일 객체 배열
   */
  @Async
  public void uploadFilesAsync(MultipartFile[] multipartFiles) {
    for (MultipartFile multipartFile : multipartFiles) {
      uploadFile(multipartFile);
    }
  }

  /**
   * 원격 서버에서 파일 삭제 (비동기)
   *
   * @param filename 삭제할 파일의 이름
   */
  @Async
  public void deleteFileAsync(String filename) {
    deleteFile(filename);
  }

  /**
   * 원격 서버에서 파일 삭제
   *
   * @param filename 삭제할 파일의 이름
   */
  public void deleteFile(String filename) {
    String remoteFilePath = ftpConfig.getDocumentPath() + "/" + filename;
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
      }
    }
  }
}
