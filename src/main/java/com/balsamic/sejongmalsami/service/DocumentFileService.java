package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.FtpUtil;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
import com.balsamic.sejongmalsami.util.TimeUtil;
import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentFileService {
  private final DocumentFileRepository documentFileRepository;
  private final FtpConfig ftpConfig;
  private final FtpUtil ftpUtil;
  private final ImageThumbnailGenerator thumbnailGenerator;

  private final int MAX_FILE_UPLOAD_SIZE = 50; // 50MB

  /**
   * 업로드 타입에 따라 파일을 저장하는 메서드
   */
  public DocumentFile saveFile(DocumentCommand command, UploadType uploadType, MultipartFile file) {
    // 파일 유효성 검증
    validateFile(file);

    // 썸네일 URL 생성
    String thumbnailUrl = generateThumbnailUrl(file, uploadType);

    // 업로드 파일명 생성
    String uploadFileName = generateUploadFileName(file);

    // MIME 타입 Enum 변환
    MimeType mimeType = MimeType.fromString(file.getContentType());

    // DocumentFile 객체 생성
    DocumentFile documentFile = DocumentFile.builder()
        .postId(command.getPostId())
        .uploader(command.getMember())
        .thumbnailUrl(thumbnailUrl)
        .originalFileName(file.getOriginalFilename())
        .uploadFileName(uploadFileName)
        .fileSize(file.getSize())
        .mimeType(mimeType)
        .downloadCount(0L)
        .password(null)
        .isInitialPasswordSet(false)
        .build();

    // 저장
    documentFileRepository.save(documentFile);

    log.info("{} 파일 저장 완료: 업로드 파일명={}", uploadType, uploadFileName);

    return documentFile;
  }

  /**
   * 파일 유효성 확인 (단일 파일)
   */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      log.error("업로드된 파일이 비어있습니다.");
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }
    if (file.getSize() > MAX_FILE_UPLOAD_SIZE * 1024 * 1024) {
      log.error("파일 크기가 초과되었습니다: 파일 크기={}MB, 최대 허용 크기={}MB",
          file.getSize() / (1024 * 1024), MAX_FILE_UPLOAD_SIZE);
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }
    try {
      MimeType.fromString(file.getContentType());
    } catch (CustomException e) {
      log.error("유효하지 않은 MIME 타입: {}", file.getContentType());
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }
  }

  /**
   * 업로드 파일명 생성 (확장자 포함)
   */
  private String generateUploadFileName(MultipartFile file) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(file.getOriginalFilename());
    String extension = FileUtil.getExtension(file.getOriginalFilename());
    return String.format("%s_%s.%s", curTimeStr, baseName, extension);
  }

  /**
   * 썸네일 URL 생성
   */
  private String generateThumbnailUrl(MultipartFile file, UploadType uploadType) {
    if (uploadType == UploadType.MUSIC) {
      // MUSIC 타입은 썸네일 생성 없이 기본 썸네일 URL 사용
      log.info("MUSIC 타입의 파일은 기본 썸네일 URL을 사용합니다.");
      return ftpConfig.getDefaultMusicThumbnailUrl();
    }

    String thumbnailFileName = generateThumbnailFileName(file.getOriginalFilename());
    String thumbnailUrl = ftpConfig.getThumbnailBaseUrl();

    try {
      if (!file.isEmpty()) {
        byte[] thumbnailBytes = generateThumbnailBytes(file, uploadType);

        if (thumbnailBytes.length > 0) {
          thumbnailUrl = ftpUtil.uploadThumbnailBytes(thumbnailBytes, thumbnailFileName);
          log.info("{} 썸네일 생성 및 업로드 완료: {}", uploadType, thumbnailFileName);
        } else {
          thumbnailUrl = getDefaultThumbnailUrl(uploadType);
          log.info("썸네일 생성 실패, 기본 썸네일 사용: {}", thumbnailUrl);
        }
      } else {
        thumbnailUrl = getDefaultThumbnailUrl(uploadType);
        log.info("파일이 비어있어 기본 썸네일 사용: {}", thumbnailUrl);
      }
    } catch (IOException e) {
      log.error("파일 {} 썸네일 생성 중 오류 발생: {}", file.getOriginalFilename(), e.getMessage());
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }

    return thumbnailUrl;
  }

  /**
   * 업로드 타입에 따른 썸네일 바이트 생성
   */
  private byte[] generateThumbnailBytes(MultipartFile file, UploadType uploadType) throws IOException {
    if (uploadType == UploadType.IMAGE) {
      return thumbnailGenerator.generateImageThumbnail(file);
    } else if (uploadType == UploadType.DOCUMENT) {
      return thumbnailGenerator.generateDocumentThumbnail(file);
    } else if (uploadType == UploadType.VIDEO) {
      return thumbnailGenerator.generateVideoThumbnail(file);
    } else {
      log.warn("알 수 없는 업로드 타입: {}", uploadType);
      throw new CustomException(ErrorCode.INVALID_UPLOAD_TYPE);
    }
  }

  /**
   * 업로드 타입에 따른 기본 썸네일 URL 반환
   */
  private String getDefaultThumbnailUrl(UploadType uploadType) {
    if (uploadType == UploadType.DOCUMENT) {
      return ftpConfig.getDefaultDocumentThumbnailUrl();
    } else if (uploadType == UploadType.IMAGE) {
      return ftpConfig.getDefaultImageThumbnailUrl();
    } else if (uploadType == UploadType.VIDEO) {
      return ftpConfig.getDefaultVideoThumbnailUrl();
    } else {
      return ftpConfig.getDefaultImageThumbnailUrl(); // 안전하게 기본 이미지 썸네일 사용
    }
  }

  /**
   * 썸네일 파일명 생성 (썸네일 확장자에 맞게)
   */
  private String generateThumbnailFileName(String originalFileName) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(originalFileName);
    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat();
    return String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
  }
}
