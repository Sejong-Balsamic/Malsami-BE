package com.balsamic.sejongmalsami.util.storage;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.ImageQuality;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import com.balsamic.sejongmalsami.util.properties.FtpProperties;
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
  private final FtpProperties ftpProperties;
  private final ImageThumbnailGenerator imageThumbnailGenerator;

  private static final int MAX_RETRY_ATTEMPTS = 3; // 최대 재시도 횟수
  private static final int RETRY_DELAY_SECONDS = 5; // 재시도 지연 시간 (초 단위)

  // 파일 Path 반환
  @Override
  @LogMonitoringInvocation
  public String uploadFile(ContentType contentType, MultipartFile multipartFile) {
    if (multipartFile.isEmpty()) {
      log.error("FTP 파일 업로드 실패: 크기가 0MB인 파일 업로드 : {}", multipartFile.getOriginalFilename());
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }

    String uploadFileName = FileUtil.generateFileName(contentType, multipartFile.getOriginalFilename());
    String remoteFilePath = getFileSavePath(contentType) + "/" + uploadFileName;

    log.info("FTP 파일 업로드 시작: {} -> {}", multipartFile.getOriginalFilename(), remoteFilePath);

    for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
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
        log.error("FTP 파일 업로드 중 예외 발생 (시도 {}): {}", attempt, e.getMessage(), e);
        if (attempt == MAX_RETRY_ATTEMPTS) {
          throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
        }
        // 재시도 로그 추가
        log.warn("FTP 파일 업로드 재시도: {}번째 시도 실패. {}초 뒤에 다시 시도합니다...", attempt, RETRY_DELAY_SECONDS);
        try {
          Thread.sleep(RETRY_DELAY_SECONDS * 1000L); // 밀리초 단위 변환
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          log.error("재시도 대기 중 인터럽트 발생", ie);
        }
      } finally {
        if (ftpClient != null) {
          ftpClientPool.returnObject(ftpClient);
          log.debug("FTPClient 반환 완료: {}", ftpClient);
        }
      }
    }
    throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR); // 재시도 실패 시 예외 던지기
  }

  // 썸네일 URL 반환
  @Override
  @LogMonitoringInvocation
  public String uploadThumbnail(ContentType contentType, MultipartFile multipartFile) {
    // 파일명 생성
    String uploadFileName = FileUtil.generateFileName(contentType, multipartFile.getOriginalFilename());

    // 파일 접근 URL
    String uploadFileUrl = ftpProperties.getBaseUrl() + contentType.name().toLowerCase() + "/" + uploadFileName;

    // contentType에 대한 업로드할 경로 지정 ->  contentType.THUMBNAIL
    String remoteFilePath = ftpProperties.getThumbnailPath() + "/" + uploadFileName;

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
    String uploadFileUrl = ftpProperties.getBaseUrl() + contentType.name().toLowerCase() + "/" + uploadFileName;

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
      case THUMBNAIL -> ftpProperties.getThumbnailPath() + "/" + fileName;
      case DOCUMENT -> ftpProperties.getDocumentPath() + "/" + fileName;
      case QUESTION, ANSWER -> ftpProperties.getQuestionPath() + "/" + fileName;
      case NOTICE -> ftpProperties.getNoticePath() + "/" + fileName;
      case COMMENT -> ftpProperties.getCommentPath() + "/" + fileName;
      case DOCUMENT_REQUEST -> ftpProperties.getDocumentRequestPath() + "/" + fileName;
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
      case DOCUMENT -> ftpProperties.getDocumentFileDevPath();
      case QUESTION -> ftpProperties.getQuestionFileDevPath();
      case ANSWER -> ftpProperties.getAnswerFileDevPath();
      case NOTICE -> ftpProperties.getNoticeFileDevPath();
      case COMMENT -> ftpProperties.getCommentFileDevPath();
      case DOCUMENT_REQUEST -> ftpProperties.getDocumentRequestFileDevPath();
      case COURSES -> ftpProperties.getCoursesFileDevPath();
      case THUMBNAIL -> ftpProperties.getThumbnailFileDevPath();
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    };
  }

  // URL 접근 가능한 파일들
  private String getWebSavePath(ContentType contentType) {
    return switch (contentType) {
      case DOCUMENT -> ftpProperties.getDocumentWebDevPath();
      case QUESTION -> ftpProperties.getQuestionWebDevPath();
      case ANSWER -> ftpProperties.getAnswerWebDevPath();
      case NOTICE -> ftpProperties.getNoticeWebDevPath();
      case COMMENT -> ftpProperties.getCommentWebDevPath();
      case DOCUMENT_REQUEST -> ftpProperties.getDocumentRequestWebDevPath();
      case COURSES -> ftpProperties.getCoursesWebDevPath();
      case THUMBNAIL -> ftpProperties.getThumbnailWebDevPath();
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    };
  }
}
