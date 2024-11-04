package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentFileService {

  private final DocumentFileRepository documentFileRepository;
  private final FtpConfig ftpConfig;
  private final FtpUtil ftpUtil;
  private final ImageThumbnailGenerator thumbnailGenerator;

  // 파일 유형별 최대 업로드 크기 (MB)
  private static final int MAX_BASIC_UPLOAD_SIZE = 50;    // 이미지, 문서 등
  private static final int MAX_VIDEO_UPLOAD_SIZE = 200;   // 비디오
  private final DocumentPostRepository documentPostRepository;

  /**
   * 파일 저장
   *
   * @param command    문서 명령어 객체
   * @param uploadType 업로드 타입
   * @param file       업로드 파일
   * @return 저장된 DocumentFile 객체
   */
  public DocumentFile saveFile(DocumentCommand command, UploadType uploadType, MultipartFile file) {
    String thumbnailUrl = generateThumbnailUrl(file, uploadType);
    String uploadFileName = FileUtil.generateUploadFileName(file);

    DocumentPost documentPost = documentPostRepository.findByDocumentPostId(command.getDocumentPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    // 첨부파일 업로드
    ftpUtil.uploadDocument(file, uploadFileName);

    // 메타 데이터 documentFile 저장
    DocumentFile savedDocumentFile = documentFileRepository.save(DocumentFile.builder()
        .documentPost(documentPost)
        .uploader(command.getMember())
        .thumbnailUrl(thumbnailUrl)
        .originalFileName(file.getOriginalFilename())
        .uploadFileName(uploadFileName)
        .fileSize(file.getSize())
        .mimeType(MimeType.fromString(file.getContentType()))
        .downloadCount(0L)
        .password(null)
        .isInitialPasswordSet(false)
        .build());
    log.info("DocumentFile 저장완료 : ID : {} ,업로드 파일명={}", savedDocumentFile.getDocumentFileId(), savedDocumentFile.getUploadFileName());
    return savedDocumentFile;
  }

  /**
   * 파일 유효성 검증
   *
   * @param file       검증할 파일
   * @param uploadType 업로드 타입
   */
  public void validateFile(MultipartFile file, UploadType uploadType) {
    if (file == null || file.isEmpty()) {
      log.error("업로드된 파일이 비어있습니다.");
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }

    long fileSizeInMB = file.getSize() / (1024 * 1024);
    int maxSize;

    // 업로드 타입에 따른 최대 파일 크기 설정 (if-else if 사용)
    if (uploadType == UploadType.VIDEO) {
      maxSize = MAX_VIDEO_UPLOAD_SIZE;
    } else if (uploadType == UploadType.IMAGE || uploadType == UploadType.DOCUMENT || uploadType == UploadType.MUSIC) {
      maxSize = MAX_BASIC_UPLOAD_SIZE;
    } else {
      log.warn("uploadType 을 알 수 없습니다. file {} , {}", file.getOriginalFilename(), uploadType);
      maxSize = MAX_BASIC_UPLOAD_SIZE;
    }

    if (fileSizeInMB > maxSize) {
      log.error("파일 크기가 초과되었습니다: 파일 크기={}MB, 최대 허용 크기={}MB", fileSizeInMB, maxSize);
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
   * 썸네일 URL 생성
   *
   * @param file       대상 파일
   * @param uploadType 업로드 타입
   * @return 썸네일 URL
   */
  private String generateThumbnailUrl(MultipartFile file, UploadType uploadType) {

    String thumbnailFileName = generateThumbnailFileName(file.getOriginalFilename());
    String thumbnailUrl = "";

    if (file.isEmpty()) {
      log.info("generateThumbnailUrl : 파일이 비어있습니다");
      return "";
    }

    if (uploadType == UploadType.MUSIC) {
      log.info("MUSIC 타입의 파일은 기본 썸네일 URL을 사용합니다.");
      return ftpConfig.getDefaultMusicThumbnailUrl();
    }

    byte[] thumbnailBytes = generateThumbnailBytes(file, uploadType);

    if (thumbnailBytes.length > 0) {
      thumbnailUrl = ftpUtil.uploadThumbnailBytes(thumbnailBytes, thumbnailFileName);
      log.info("{} 썸네일 생성 및 업로드 완료: {}", uploadType, thumbnailFileName);
    } else {
      thumbnailUrl = getDefaultThumbnailUrl(uploadType);
      log.info("썸네일 생성 실패, 기본 썸네일 사용: {}", thumbnailUrl);
    }
    return thumbnailUrl;
  }

  /**
   * 썸네일 바이트 생성
   *
   * @param file       대상 파일
   * @param uploadType 업로드 타입
   * @return 썸네일 바이트 배열
   * @throws CustomException ErrorCode.INVALID_UPLOAD_TYPE
   */
  private byte[] generateThumbnailBytes(MultipartFile file, UploadType uploadType) throws CustomException {
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
   * 기본 썸네일 URL 반환
   *
   * @param uploadType 업로드 타입
   * @return 기본 썸네일 URL
   */
  private String getDefaultThumbnailUrl(UploadType uploadType) {
    if (uploadType == UploadType.DOCUMENT) {
      return ftpConfig.getDefaultDocumentThumbnailUrl();
    } else if (uploadType == UploadType.IMAGE) {
      return ftpConfig.getDefaultImageThumbnailUrl();
    } else if (uploadType == UploadType.VIDEO) {
      return ftpConfig.getDefaultVideoThumbnailUrl();
    } else {
      log.warn("기본 썸네일이 존재하지 않습니다. UploadType={}", uploadType);
      return "";
    }
  }

  /**
   * 썸네일 파일명 생성
   *
   * @param originalFileName 원본 파일명
   * @return 생성된 썸네일 파일명
   */
  private String generateThumbnailFileName(String originalFileName) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(originalFileName);
    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat(); // JPG, WEBP
    return String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
  }
}
