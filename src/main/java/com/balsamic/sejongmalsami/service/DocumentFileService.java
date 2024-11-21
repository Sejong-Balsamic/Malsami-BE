package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
import com.balsamic.sejongmalsami.util.storage.StorageService;
import com.balsamic.sejongmalsami.util.TimeUtil;
import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentFileService {

  private final DocumentFileRepository documentFileRepository;
  private final DocumentPostRepository documentPostRepository;

  @Qualifier("ftpStorageService")
  private final StorageService storageService;

  private final ImageThumbnailGenerator imageThumbnailGenerator;
  private final FtpConfig ftpConfig;

  // 파일 유형별 최대 업로드 크기 (MB)
  private static final int MAX_BASIC_UPLOAD_SIZE = 50;    // 이미지, 문서 등
  private static final int MAX_VIDEO_UPLOAD_SIZE = 200;   // 비디오

  /**
   * 파일 저장
   *
   * @param command    문서 명령어 객체
   * @param uploadType 업로드 타입
   * @param file       업로드 파일
   * @return 저장된 DocumentFile 객체
   */
  @Transactional
  public DocumentFile saveFile(DocumentCommand command, UploadType uploadType, MultipartFile file) {
    // 파일 유효성 검증
    validateFile(file, uploadType);

    // 파일 업로드
    String fileUrl = storageService.uploadFile(ContentType.DOCUMENT, file); // MEDIA 경로에 업로드

    DocumentPost documentPost = documentPostRepository.findById(command.getDocumentPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    // 썸네일 URL 생성
    String thumbnailUrl = generateThumbnailUrl(ContentType.DOCUMENT, file, uploadType);

    // 메타 데이터 documentFile 저장
    DocumentFile savedDocumentFile = documentFileRepository.save(DocumentFile.builder()
        .documentPost(documentPost)
        .uploader(command.getMember())
        .thumbnailUrl(thumbnailUrl)
        .originalFileName(file.getOriginalFilename())
        .uploadFileName(fileUrl)
        .fileSize(file.getSize())
        .mimeType(MimeType.fromString(file.getContentType()))
        .downloadCount(0L)
        .password(null)
        .isInitialPasswordSet(false)
        .filePath(fileUrl)
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

    // 업로드 타입에 따른 최대 파일 크기 설정
    if (uploadType == UploadType.VIDEO) {
      maxSize = MAX_VIDEO_UPLOAD_SIZE;
    } else {
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
   * @param contentType ContentType
   * @param file        대상 파일
   * @param uploadType  업로드 타입
   * @return 썸네일 URL
   */
  private String generateThumbnailUrl(ContentType contentType, MultipartFile file, UploadType uploadType) {
    if (file.isEmpty()) {
      log.info("generateThumbnailUrl : 파일이 비어있습니다");
      return "";
    }

    if (uploadType == UploadType.MUSIC) {
      log.info("MUSIC 타입의 파일은 기본 썸네일 URL을 사용합니다.");
      return ftpConfig.getDefaultMusicThumbnailUrl(); // 기본 썸네일 URL 반환
    }

    try {
      // 썸네일 생성 및 업로드
      return createAndUploadThumbnail(contentType, file);
    } catch (Exception e) {
      log.error("썸네일 생성 실패: {}", e.getMessage(), e);
      // 업로드 타입에 따른 기본 썸네일 URL 사용
      return getDefaultThumbnailUrl(uploadType);
    }
  }

  /**
   * 썸네일 생성 및 업로드
   *
   * @param contentType ContentType
   * @param file        대상 파일
   * @return 업로드된 썸네일 URL
   */
  private String createAndUploadThumbnail(ContentType contentType, MultipartFile file) {
    // MIME 타입에 따라 적절한 썸네일 생성 메서드 호출
    byte[] thumbnailBytes;
    String mimeType = file.getContentType();

    if (MimeType.isValidImageMimeType(mimeType)) {
      thumbnailBytes = imageThumbnailGenerator.generateImageThumbnail(file);
    } else if (MimeType.isValidDocumentMimeType(mimeType)) {
      thumbnailBytes = imageThumbnailGenerator.generateDocumentThumbnail(file);
    } else if (mimeType.startsWith("video/")) {
      thumbnailBytes = imageThumbnailGenerator.generateVideoThumbnail(file);
    } else {
      log.warn("지원되지 않는 MIME 타입으로 인해 썸네일 생성을 건너뜁니다: {}", mimeType);
      return "";
    }

    // 썸네일 파일 생성 (MultipartFileAdapter 사용)
    MultipartFile thumbnailFile = new ImageThumbnailGenerator.MultipartFileAdapter(
        generateThumbnailFileName(contentType, file.getOriginalFilename()),
        thumbnailBytes
    );

    // 썸네일 업로드
    return storageService.uploadThumbnail(ContentType.THUMBNAIL, thumbnailFile);
  }

  /**
   * 업로드 타입에 따른 기본 썸네일 URL 반환
   *
   * @param uploadType 업로드 타입
   * @return 기본 썸네일 URL
   */
  private String getDefaultThumbnailUrl(UploadType uploadType) {
    switch (uploadType) {
      case DOCUMENT:
        return ftpConfig.getDefaultDocumentThumbnailUrl();
      case IMAGE:
        return ftpConfig.getDefaultImageThumbnailUrl();
      case VIDEO:
        return ftpConfig.getDefaultVideoThumbnailUrl();
      case MUSIC:
        return ftpConfig.getDefaultMusicThumbnailUrl();
      default:
        return ""; // 필요 시 기본값 설정
    }
  }

  /**
   * 썸네일 파일명 생성
   *
   * @param contentType      ContentType
   * @param originalFileName 원본 파일명
   * @return 생성된 썸네일 파일명
   */
  private String generateThumbnailFileName(ContentType contentType, String originalFileName) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(originalFileName);
    String thumbnailExtension = "jpg"; // 또는 실제 썸네일 생성 시 사용한 형식
    return String.format("%s_%s_%s.%s", baseName, curTimeStr, "thumbnail", thumbnailExtension);
  }
}
