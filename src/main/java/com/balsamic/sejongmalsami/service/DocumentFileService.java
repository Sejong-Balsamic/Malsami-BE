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
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;
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

  private final int MAX_FILE_UPLOAD_SIZE = 50; // 50MB
  private final WebInvocationPrivilegeEvaluator privilegeEvaluator;

  public DocumentFile saveDocumentFile(DocumentCommand command) {
    MultipartFile file = command.getFile();
    validateFile(command);

    String uploadFileName = generateUploadFileName(file.getOriginalFilename());
    String thumbnailUrl = generateThumbnailUrl(command, file, uploadFileName);

    return documentFileRepository.save(
        DocumentFile.builder()
            .postId(command.getPostId())
            .uploader(command.getMember())
            .thumbnailUrl(thumbnailUrl)
            .originalFileName(file.getOriginalFilename())
            .uploadFileName(uploadFileName)
            .fileSize(file.getSize())
            .mimeType(MimeType.fromString(file.getContentType()))
            .downloadCount(0L)
            .password(null)
            .isInitialPasswordSet(false)
            .build()
    );
  }

  public DocumentFile saveMediaFile(DocumentCommand command) {
    // 미디어 파일 처리 로직이 다르다면 여기에 구현
    // 현재는 saveDocumentFile과 유사하게 처리하므로, 동일한 로직을 사용할 수 있습니다.
    return saveDocumentFile(command);
  }

  public DocumentFile saveImagesToDocumentFile(DocumentCommand command) {
    // TODO: 이미지 리스트를 ZIP으로 변환하고 FTP에 업로드 후 DocumentFile 저장
    // 현재는 구현되지 않았으므로, 임시로 null 반환
    // 추후 구현 시 로직 추가
    return null;
  }

  private void validateFile(DocumentCommand command) {
    MultipartFile file = command.getFile();
    if (file == null || file.isEmpty()) {
      log.error("업로드된 파일이 비어있습니다.");
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }
    if (file.getSize() > MAX_FILE_UPLOAD_SIZE * 1024 * 1024) {
      log.error("파일 크기가 초과되었습니다: 파일 크기={}MB, 최대 허용 크기={}MB",
          file.getSize() / (1024 * 1024), MAX_FILE_UPLOAD_SIZE);
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }
    if (command.getUploadType() == null) {
      log.error("uploadType이 설정되지 않았습니다.");
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  private String generateUploadFileName(String originalFileName) {
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(originalFileName);
    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat();
    return String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
  }

  private String generateThumbnailUrl(DocumentCommand command, MultipartFile file, String uploadFileName) {
    String thumbnailUrl = ftpConfig.getBaseImageUrl();
    try {
      if (!file.isEmpty()) {
        UploadType uploadType = command.getUploadType();

        if (uploadType == UploadType.IMAGE) {
          thumbnailUrl = ftpUtil.uploadThumbnailBytes(
              thumbnailGenerator.generateImageThumbnail(file), uploadFileName);
          log.info("이미지 썸네일 생성 및 업로드 완료: {}", uploadFileName);
        } else if (uploadType == UploadType.DOCUMENT) {
          thumbnailUrl = ftpUtil.uploadThumbnailBytes(
              thumbnailGenerator.generateDocumentThumbnail(file), uploadFileName);
          log.info("문서 썸네일 생성 및 업로드 완료: {}", uploadFileName);
        } else if (uploadType == UploadType.MEDIA) {
          // TODO: 동영상 썸네일 생성 로직 추가
        } else {
          log.warn("알 수 없는 업로드 타입: {}", uploadType);
        }
      }
    } catch (IOException e) {
      log.error("파일 {} 썸네일 생성 중 오류 발생: {}", file.getOriginalFilename(), e.getMessage());
      throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
    }
    return thumbnailUrl;
  }

}
