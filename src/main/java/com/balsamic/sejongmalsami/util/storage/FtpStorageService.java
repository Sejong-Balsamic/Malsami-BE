package com.balsamic.sejongmalsami.util.storage;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ImageQuality;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
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
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * FTP 스토리지 서비스 구현 클래스
 */
@Primary
@Service("ftpStorageService")
@Slf4j
@RequiredArgsConstructor
public class FtpStorageService implements StorageService {

  private final GenericObjectPool<FTPClient> ftpClientPool;
  private final FtpConfig ftpConfig;
  private final ImageThumbnailGenerator imageThumbnailGenerator;

  // 파일 Path 반환
  @Override
  @LogMonitoringInvocation
  public String uploadFile(ContentType contentType, MultipartFile multipartFile) {
    String uploadFileName = FileUtil.generateFileName(contentType, multipartFile.getOriginalFilename());
    String remoteFilePath = getFileSavePath(contentType) + "/" + uploadFileName;

    log.info("FTP 파일 업로드 시작: {} -> {}", multipartFile.getOriginalFilename(), remoteFilePath);

    FTPClient ftpClient = null;
    try {
      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = multipartFile.getInputStream()) {
        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (success) {
          log.info("FTP 파일 업로드 성공: {}", remoteFilePath);
          return remoteFilePath;
        } else {
          String reply = ftpClient.getReplyString();
          log.error("FTP 파일 업로드 실패: {}. 서버 응답: {}", remoteFilePath, reply);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (CustomException ce) {
      throw ce; // 커스텀 예외는 다시 던집니다.
    } catch (Exception e) {
      log.error("FTP 파일 업로드 중 예외 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
        log.debug("FTPClient 반환 완료: {}", ftpClient);
      }
    }
  }

  // 썸네일 URL 반환
  @Override
  @LogMonitoringInvocation
  public String uploadThumbnail(ContentType contentType, MultipartFile multipartFile) {
    // 파일명 생성
    String uploadFileName = FileUtil.generateFileName(contentType, multipartFile.getOriginalFilename());

    // 파일 접근 URL
    String uploadFileUrl = ftpConfig.getBaseUrl() + contentType.name().toLowerCase() + "/" + uploadFileName;

    // contentType에 대한 업로드할 경로 지정 ->  contentType.THUMBNAIL
    String remoteFilePath = ftpConfig.getThumbnailPath() + "/" + uploadFileName;

    log.info("FTP 썸네일 업로드 시작: {} -> {}", multipartFile.getOriginalFilename(), remoteFilePath);

    FTPClient ftpClient = null;
    try {
      // 썸네일 변환
      byte[] generatedImageBytes = imageThumbnailGenerator.generateImageThumbnail(multipartFile);

      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = new ByteArrayInputStream(generatedImageBytes)) {
        // 썸네일 path 에 도입
        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (success) {
          log.info("FTP 썸네일 업로드 성공: {}", remoteFilePath);
          log.info("FTP 썸네일 접근 URL : {}", uploadFileUrl);
          return uploadFileUrl;
        } else {
          String reply = ftpClient.getReplyString();
          log.error("FTP 썸네일 업로드 실패: {}. 서버 응답: {}", remoteFilePath, reply);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (Exception e) {
      log.error("FTP 썸네일 업로드 중 예외 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
        log.debug("FTPClient 반환 완료: {}", ftpClient);
      }
    }
  }

  @Override
  public String uploadImage(ContentType contentType, MultipartFile multipartFile) {
    // 파일명 생성
    String uploadFileName = FileUtil.generateFileName(contentType, multipartFile.getOriginalFilename());

    // 파일 접근 URL
    String uploadFileUrl = ftpConfig.getBaseUrl() + contentType.name().toLowerCase() + "/" + uploadFileName;

    // contentType 에 대한 업로드할 경로 지정
    String remoteFilePath = getWebSavePath(contentType) + "/" + uploadFileName;

    log.info("FTP 썸네일 업로드 시작: {} -> {}", multipartFile.getOriginalFilename(), remoteFilePath);

    FTPClient ftpClient = null;
    try {
      // 이미지 압축 생성
      byte[] generatedImageBytes = imageThumbnailGenerator.generateImageCompress(multipartFile, ImageQuality.MEDIUM);

      ftpClient = ftpClientPool.borrowObject();
      try (InputStream inputStream = new ByteArrayInputStream(generatedImageBytes)) {
        // 썸네일 path 에 도입
        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (success) {
          log.info("FTP 이미지 업로드 성공: {}", remoteFilePath);
          log.info("FTP 이미지 접근 URL : {}", uploadFileUrl);
          return uploadFileUrl;
        } else {
          String reply = ftpClient.getReplyString();
          log.error("FTP 썸네일 업로드 실패: {}. 서버 응답: {}", remoteFilePath, reply);
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
      }
    } catch (Exception e) {
      log.error("FTP 썸네일 업로드 중 예외 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        ftpClientPool.returnObject(ftpClient);
        log.debug("FTPClient 반환 완료: {}", ftpClient);
      }
    }
  }

  @Override
  @LogMonitoringInvocation
  public void deleteFile(ContentType contentType, String fileName) {
    String remoteFilePath = switch (contentType) {
      case THUMBNAIL -> ftpConfig.getThumbnailPath() + "/" + fileName;
      case DOCUMENT -> ftpConfig.getDocumentPath() + "/" + fileName;
      case QUESTION, ANSWER -> ftpConfig.getQuestionPath() + "/" + fileName;
      case NOTICE -> ftpConfig.getNoticePath() + "/" + fileName;
      case COMMENT -> ftpConfig.getCommentPath() + "/" + fileName;
      case DOCUMENT_REQUEST -> ftpConfig.getDocumentRequestPath() + "/" + fileName;
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
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
      log.error("FTP 파일 삭제 중 예외 발생: {}", e.getMessage(), e);
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

  // URL 접근 불가능
  private String getFileSavePath(ContentType contentType) {
    return switch (contentType) {
      case DOCUMENT -> ftpConfig.getDocumentFileDevPath();
      case QUESTION -> ftpConfig.getQuestionFileDevPath();
      case ANSWER -> ftpConfig.getAnswerFileDevPath();
      case NOTICE -> ftpConfig.getNoticeFileDevPath();
      case COMMENT -> ftpConfig.getCommentFileDevPath();
      case DOCUMENT_REQUEST -> ftpConfig.getDocumentRequestFileDevPath();
      case COURSES -> ftpConfig.getCoursesFileDevPath();
      case THUMBNAIL -> ftpConfig.getThumbnailFileDevPath();
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    };
  }

  // URL 접근 가능한 파일들
  private String getWebSavePath(ContentType contentType) {
    return switch (contentType) {
      case DOCUMENT -> ftpConfig.getDocumentWebDevPath();
      case QUESTION -> ftpConfig.getQuestionWebDevPath();
      case ANSWER -> ftpConfig.getAnswerWebDevPath();
      case NOTICE -> ftpConfig.getNoticeWebDevPath();
      case COMMENT -> ftpConfig.getCommentWebDevPath();
      case DOCUMENT_REQUEST -> ftpConfig.getDocumentRequestWebDevPath();
      case COURSES -> ftpConfig.getCoursesWebDevPath();
      case THUMBNAIL -> ftpConfig.getThumbnailWebDevPath();
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    };
  }
}