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
import com.balsamic.sejongmalsami.util.MultipartFileAdapter;
import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.storage.StorageService;
import java.io.IOException;
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
   * 단일 파일 저장 (MAIN)
   * 1. 파일 유효성 검증
   * 2. 파일 업로드 및 경로 생성
   * 3. 썸네일 생성 및 URL 저장
   * 4. 게시글 검증
   * 5. DB에 파일 메타데이터 저장
   * @return 저장된 DocumentFile 객체
   */
  @Transactional
  public DocumentFile saveFile(DocumentCommand command, UploadType uploadType, MultipartFile file) {
    // 파일 유효성 검증
    validateFile(file, uploadType);

    // 파일 업로드 및 파일 Path 반환
    String filePath = storageService.uploadFile(ContentType.DOCUMENT, file);

    // 썸네일 업로드 및 URL 생성
    String thumbnailUrl = generateThumbnailUrl(ContentType.DOCUMENT, file, uploadType);

    // 자료 게시글 검증
    DocumentPost documentPost = documentPostRepository.findById(command.getDocumentPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    // 메타 데이터 documentFile 저장
    DocumentFile savedDocumentFile = documentFileRepository.save(DocumentFile.builder()
        .documentPost(documentPost)
        .uploader(command.getMember())
        .thumbnailUrl(thumbnailUrl)
        .originalFileName(file.getOriginalFilename())
        .uploadedFileName(FileUtil.extractFileName(filePath))
        .fileSize(file.getSize())
        .mimeType(MimeType.fromString(file.getContentType()))
        .downloadCount(0L)
        .password(null)
        .isInitialPasswordSet(false)
        .filePath(filePath)
        .build());
    log.info("DocumentFile 저장완료 : ID : {} ,업로드 파일명={}", savedDocumentFile.getDocumentFileId(), savedDocumentFile.getUploadedFileName());
    return savedDocumentFile;
  }

  /**
   * 파일 유효성 검증
   * 1. 빈 파일 체크
   * 2. 파일 크기 제한 (일반:50MB, 영상:200MB)
   * 3. MIME 타입 검증
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
   * 1. 음원 파일 -> 기본 썸네일 사용
   * 2. 나머지 -> 썸네일 생성 시도
   * 3. 실패시 타입별 기본 썸네일 반환
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
   * 실제 썸네일 생성 및 업로드
   * 1. WebP 파일 특별 처리
   * 2. MIME 타입별 썸네일 생성
   * 3. 생성된 썸네일 업로드
   */
  private String createAndUploadThumbnail(ContentType contentType, MultipartFile file) {
    byte[] thumbnailBytes;
    String mimeType = file.getContentType();

    // WebP 파일 처리 : MAC 환경에서는 WepP 를 다루지 않음 (라이브러리 지원안함)
    if (MimeType.WEBP.getMimeType().equalsIgnoreCase(mimeType)) {
      log.info("WebP 파일 처리: 원본 데이터 업로드");
      try {
        return storageService.uploadThumbnail(
            ContentType.THUMBNAIL,
            new MultipartFileAdapter(
                "thumbnail",
                imageThumbnailGenerator.generateThumbnailFileName(contentType, file.getOriginalFilename()),
                MimeType.WEBP.getMimeType(),
                file.getBytes()
            )
        );
      } catch (IOException e) {
        log.error("WebP 파일 원본 데이터 처리 중 오류: {}", e.getMessage(), e);
        throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
      }
    }

    // MIME 타입에 따라 썸네일 생성
    if (MimeType.isValidImageMimeType(mimeType)) {
      thumbnailBytes = imageThumbnailGenerator.generateImageThumbnail(file);
    } else if (MimeType.isValidDocumentMimeType(mimeType)) {
      thumbnailBytes = imageThumbnailGenerator.generateDocumentThumbnail(file);
    } else if (mimeType.startsWith("video/")) {
      thumbnailBytes = imageThumbnailGenerator.generateVideoThumbnail(file);
    } else {
      log.warn("지원되지 않는 MIME 타입: {}", mimeType);
      return "";
    }

    // 생성된 썸네일 파일 업로드
    String thumbnailFileName = imageThumbnailGenerator.generateThumbnailFileName(contentType, file.getOriginalFilename());
    MimeType thumbnailMimeType = imageThumbnailGenerator.getOutputThumbnailMimeType();
    MultipartFile thumbnailFile = new MultipartFileAdapter(
        "thumbnail",
        thumbnailFileName,
        thumbnailMimeType.getMimeType(),
        thumbnailBytes
    );

    log.debug("업로드 대상 썸네일 파일 이름: {}, MIME 타입: {}", thumbnailFileName, thumbnailMimeType.getMimeType());
    return storageService.uploadThumbnail(ContentType.THUMBNAIL, thumbnailFile);
  }

  /**
   * 업로드 타입에 따른 기본 썸네일 URL 반환
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
        return ""; // 빈 URL 설정 (프론트가 해주시겠지)
    }
  }
}
