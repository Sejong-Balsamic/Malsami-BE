package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.TestCommand;
import com.balsamic.sejongmalsami.object.TestDto;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.FtpUtil;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
import com.balsamic.sejongmalsami.util.TimeUtil;
import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class TestService {

  private final FtpConfig ftpConfig;
  private final FtpUtil ftpUtil;
  private final ImageThumbnailGenerator thumbnailGenerator;
  private final ImageThumbnailGenerator imageThumbnailGenerator;

  private final int MAX_FILE_UPLOAD_SIZE = 50; // 50MB

  public TestDto saveDocumentThumbnail(TestCommand command) {
    MultipartFile file = command.getDocumentFile();

    // 파일 유효성 검사
    if (file == null || file.isEmpty()) {
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }
    // 파일 크기 검사 (예: 50MB 이하)
    if (file.getSize() > MAX_FILE_UPLOAD_SIZE * 1024 * 1024) {
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    // 기본 이미지 URL 정의
    String documentFileUrl = ftpConfig.getBaseImageUrl();

    // 파일 이름 처리
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(file.getOriginalFilename());
    String thumbnailExtension = imageThumbnailGenerator.getOutputThumbnailFormat();

    String fileName = String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);

    if(!file.isEmpty()){
      try {
        documentFileUrl = ftpUtil.uploadThumbnailBytes(
            imageThumbnailGenerator.generateDocumentThumbnail(file), fileName);
      } catch (IOException e) {
        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
      }
    }

    return TestDto.builder()
        .fileUrl(documentFileUrl)
        .build();
  }

  public TestDto saveImagesThumbnail(TestCommand command){
    List<MultipartFile> imageFiles = command.getImageFiles();

    // 첫번째 이미지로 썸네일
    MultipartFile targetImageFile = imageFiles.get(0);

    // 파일 유효성 검사
    if (targetImageFile == null || targetImageFile.isEmpty()) {
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }
    // 파일 크기 검사 (예: 50MB 이하)
    if (targetImageFile.getSize() > MAX_FILE_UPLOAD_SIZE * 1024 * 1024) {
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    // 기본 이미지 URL 정의
    String imageFileUrl = ftpConfig.getBaseImageUrl();

    // 파일 이름 처리
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
    String baseName = FileUtil.getBaseName(targetImageFile.getOriginalFilename());
    String thumbnailExtension = imageThumbnailGenerator.getOutputThumbnailFormat();

    String fileName = String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);

    // 파일 업로드
    if(!imageFiles.isEmpty()) {
      try {
        imageFileUrl = ftpUtil.uploadThumbnailBytes(
                thumbnailGenerator.generateImageThumbnail(targetImageFile), fileName);
      } catch (IOException e) {
        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
      }
    }
    return TestDto.builder()
        .fileUrl(imageFileUrl)
        .build();
  }
}
