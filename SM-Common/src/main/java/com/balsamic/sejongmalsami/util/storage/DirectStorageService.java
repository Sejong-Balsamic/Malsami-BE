package com.balsamic.sejongmalsami.util.storage;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.properties.FtpProperties;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
public class DirectStorageService implements StorageService {
  private final FtpProperties ftpProperties;

  @Override
  public String uploadFile(ContentType contentType, MultipartFile file) {
    String fileName = FileUtil.generateFileName(contentType, file.getOriginalFilename());
    String path = getPath(contentType);
    File destFile = new File(path, fileName);

    try {
      destFile.getParentFile().mkdirs();
      file.transferTo(destFile);
      log.info("파일 업로드 성공: {}", destFile.getAbsolutePath());
      return destFile.getAbsolutePath();
    } catch (IOException e) {
      log.error("파일 업로드 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.DIRECT_FILE_UPLOAD_ERROR);
    }
  }

  @Override
  public String uploadThumbnail(ContentType contentType, MultipartFile file) {
    String fileName = FileUtil.generateFileName(contentType, file.getOriginalFilename());
    String path = ftpProperties.getThumbnailPath();
    File destFile = new File(path, fileName);

    try {
      destFile.getParentFile().mkdirs();
      file.transferTo(destFile);
      log.info("썸네일 업로드 성공: {}", destFile.getAbsolutePath());
      //FIXME: 임시 반환
      return ftpProperties.getBaseUrl() + fileName;
    } catch (IOException e) {
      log.error("썸네일 업로드 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.DIRECT_FILE_UPLOAD_ERROR);
    }
  }

  @Override
  public String uploadImage(ContentType contentType, MultipartFile file) {
    //TODO: 미구현
    return "미구현";
  }

  @Override
  public void deleteFile(ContentType contentType, String fileUrl) {
    String fileName = extractFileName(fileUrl);
    String path = getPath(contentType);
    File file = new File(path, fileName);

    if (file.exists() && !file.delete()) {
      log.error("파일 삭제 실패: {}", file.getAbsolutePath());
      throw new CustomException(ErrorCode.DIRECT_FILE_DELETE_ERROR);
    }
    log.info("파일 삭제 성공: {}", file.getAbsolutePath());
  }

  private String getPath(ContentType contentType) {
    return switch (contentType) {
      case DOCUMENT -> ftpProperties.getDocumentPath();
      case QUESTION -> ftpProperties.getQuestionPath();
      case ANSWER -> ftpProperties.getAnswerPath();
      case NOTICE -> ftpProperties.getNoticePath();
      case COMMENT -> ftpProperties.getCommentPath();
      case DOCUMENT_REQUEST -> ftpProperties.getDocumentRequestPath();
      case COURSES -> ftpProperties.getCoursesPath();
      case THUMBNAIL -> ftpProperties.getThumbnailPath();
      default -> throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    };
  }

  private String extractFileName(String fileUrl) {
    return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
  }
}