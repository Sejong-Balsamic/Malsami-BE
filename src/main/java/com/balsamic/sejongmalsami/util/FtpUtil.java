package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class FtpUtil {

  private final GenericObjectPool<FTPClient> ftpClientPool;
  private final FtpConfig ftpConfig;

  /**
   * FTP를 통해 단일 파일 업로드 (비동기)
   *
   * @param file 업로드할 멀티파트 파일 객체
   */
  @Async
  public void uploadDocumentAsync(MultipartFile file) {
    uploadDocument(file, FileUtil.generateUploadFileName(file));
  }

  /**
   * FTP를 통해 단일 파일 업로드
   * @param file
   * @param uploadFileName
   * @return uploadFileName
   */
  @LogMonitoringInvocation
  public String uploadDocument(MultipartFile file, String uploadFileName) {
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

  /**
   * FTP를 통해 여러 파일 업로드 (비동기)
   *
   * @param multipartFiles 업로드할 멀티파트 파일 객체 배열
   */
  @Async
  public void uploadFilesAsync(MultipartFile[] multipartFiles) {
    for (MultipartFile file : multipartFiles) {
      // 업로드 파일명 생성
      uploadDocument(file, FileUtil.generateUploadFileName(file));
    }
  }

  /**
   * 원격 서버에서 파일 삭제 (비동기)
   *
   * @param filename 삭제할 파일의 이름
   */
  @Async
  public void deleteDocumentAsync(String filename) {
    deleteDocument(filename);
  }

  /**
   * 원격 서버에서 파일 삭제
   *
   * @param filename 삭제할 파일의 이름
   */
  @LogMonitoringInvocation
  public void deleteDocument(String filename) {
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
        log.debug("FTPClient 반환: {}", ftpClient);
        ftpClientPool.returnObject(ftpClient);
      }
    }
  }

  /**
   * FTP를 통해 단일 썸네일 파일 업로드 (비동기)
   *
   * @param multipartFile 업로드할 멀티파트 파일 객체
   */
  @Async
  public void uploadThumbnailAsync(MultipartFile multipartFile) {
    uploadThumbnail(multipartFile);
  }

  /**
   * FTP를 통해 단일 썸네일 파일 업로드
   *
   * @param multipartFile 업로드할 멀티파트 파일 객체
   */
  @LogMonitoringInvocation
  public void uploadThumbnail(MultipartFile multipartFile) {
    String remoteFile = ftpConfig.getThumbnailPath() + "/" + multipartFile.getOriginalFilename();
    log.info("FTP 썸네일 파일 업로드 시작: {} -> {}", multipartFile.getOriginalFilename(), remoteFile);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = multipartFile.getInputStream()) {
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        if (success) {
          log.info("FTP 썸네일 파일 업로드 성공: {}", remoteFile);
        } else {
          log.error("FTP 썸네일 파일 업로드 실패: {}", remoteFile);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (Exception e) {
      log.error("FTP 썸네일 파일 업로드 중 예외 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
      }
    }
  }

  /**
   * FTP를 통해 byte 배열로 된 썸네일 파일을 업로드하고 URL을 반환합니다.
   *
   * @param thumbnailBytes 업로드할 썸네일의 byte 배열
   * @param filename 업로드할 썸네일 파일의 이름
   * @return 업로드된 썸네일 파일의 URL
   */
  @Async
  public void uploadThumbnailBytesAsync(byte[] thumbnailBytes, String filename) {
    uploadThumbnailBytes(thumbnailBytes, filename);
  }

  @LogMonitoringInvocation
  public String uploadThumbnailBytes(byte[] thumbnailBytes, String filename) {
    String remoteFile = ftpConfig.getThumbnailPath() + "/" + filename;
    String uploadedThumbnailUrl = ftpConfig.getThumbnailBaseUrl() + filename;
    log.info("FTP 썸네일 파일 업로드 시작: {} -> {}", filename, remoteFile);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = new ByteArrayInputStream(thumbnailBytes)) {
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        if (success) {
          log.info("FTP 썸네일 파일 업로드 성공: {}", remoteFile);
          log.info("업로드 썸네일 주소: {}", uploadedThumbnailUrl);
          return uploadedThumbnailUrl;
        } else {
          log.error("FTP 썸네일 파일 업로드 실패: {}", remoteFile);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (Exception e) {
      log.error("FTP 썸네일 파일 업로드 중 예외 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
      }
    }
  }

  /**
   * FTP를 통해 byte 배열로 된 파일을 업로드하고 URL을 반환합니다.
   *
   * @param fileBytes 업로드할 파일의 byte 배열
   * @param filename 업로드할 파일의 이름
   * @return 업로드된 파일의 URL
   */
  @LogMonitoringInvocation
  public String uploadBytes(byte[] fileBytes, String filename) {
    String remoteFile = ftpConfig.getDocumentPath() + "/" + filename;
    String uploadedFileUrl = ftpConfig.getDocumentBaseUrl() + filename;
    log.info("FTP 파일 업로드 시작: {} -> {}", filename, remoteFile);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
        boolean success = ftpClient.storeFile(remoteFile, inputStream);
        if (success) {
          log.info("FTP 파일 업로드 성공: {}", remoteFile);
          log.info("업로드 파일 주소: {}", uploadedFileUrl);
          return uploadedFileUrl;
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
}
