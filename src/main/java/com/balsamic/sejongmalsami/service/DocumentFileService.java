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
import com.balsamic.sejongmalsami.util.StorageService;
import com.balsamic.sejongmalsami.util.TimeUtil;
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
    String contentType = ContentType.DOCUMENT.name();
    String uploadFileName = storageService.uploadFile(contentType, file);

    DocumentPost documentPost = documentPostRepository.findById(command.getDocumentPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    // 메타 데이터 documentFile 저장
    DocumentFile savedDocumentFile = documentFileRepository.save(DocumentFile.builder()
        .documentPost(documentPost)
        .uploader(command.getMember())
        .thumbnailUrl(generateThumbnailUrl(ContentType.DOCUMENT, file, uploadType))
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
    String thumbnailFileName = generateThumbnailFileName(contentType, file.getOriginalFilename());
    String thumbnailUrl = "";

    if (file.isEmpty()) {
      log.info("generateThumbnailUrl : 파일이 비어있습니다");
      return "";
    }

    if (uploadType == UploadType.MUSIC) {
      log.info("MUSIC 타입의 파일은 기본 썸네일 URL을 사용합니다.");
      return ""; // 기본 썸네일 URL 반환 로직 추가 필요
    }

    // 썸네일 생성 및 업로드 로직 추가
    // 예: thumbnailGenerator.generateThumbnail(file)
    //       storageService.uploadFile("thumbnailContentType", thumbnailBytes)
    // 여기서는 간단히 빈 문자열로 반환
    return thumbnailUrl;
  }

  /**
   * 썸네일 파일명 생성
   */
  private String generateThumbnailFileName(ContentType contentType, String originalFileName) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(originalFileName);
    String thumbnailExtension = "jpg";
    return String.format("%s_%s_%s.%s", contentType.name(), curTimeStr, baseName, thumbnailExtension);
  }
}
