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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

  public TestDto saveDocumentThumbnail(TestCommand command) {
    MultipartFile file = command.getDocumentFile();
    return null;
  }

  public TestDto saveImagesThumbnail(TestCommand command){
    List<MultipartFile> imageFiles = command.getImageFiles();

    // 첫번째 이미지로 썸네일
    MultipartFile targetImageFile = imageFiles.get(0);

    // 임시 파일 이름
    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();

    // 기본 이미지 URL 정의
    String imageFileUrl = ftpConfig.getBaseImageUrl();

    String fileName = curTimeStr + "_" + targetImageFile.getOriginalFilename();

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
