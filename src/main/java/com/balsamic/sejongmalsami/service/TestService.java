//package com.balsamic.sejongmalsami.service;
//
//import com.balsamic.sejongmalsami.object.TestCommand;
//import com.balsamic.sejongmalsami.object.TestDto;
//import com.balsamic.sejongmalsami.object.constants.MimeType;
//import com.balsamic.sejongmalsami.util.FtpUtil;
//import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
//import com.balsamic.sejongmalsami.util.TimeUtil;
//import com.balsamic.sejongmalsami.util.FileUtil;
//import com.balsamic.sejongmalsami.util.config.FtpConfig;
//import com.balsamic.sejongmalsami.util.exception.CustomException;
//import com.balsamic.sejongmalsami.util.exception.ErrorCode;
//import java.io.IOException;
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//@RequiredArgsConstructor
//public class TestService {
//
//  private final FtpConfig ftpConfig;
//  private final FtpUtil ftpUtil;
//  private final ImageThumbnailGenerator thumbnailGenerator;
//
//  private final int MAX_FILE_UPLOAD_SIZE = 50; // 50MB
//
//  /**
//   * 문서 썸네일 저장 메서드
//   *
//   * @param file 업로드할 문서 파일
//   * @return 업로드된 썸네일의 URL
//   */
//  public TestDto saveDocumentThumbnail(TestCommand command) {
//    MultipartFile file = command.getFile();
//    // 파일 유효성 검사
//    validateFile(file);
//
//    // 파일 이름 처리
//    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
//    String baseName = FileUtil.getBaseName(file.getOriginalFilename());
//    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat();
//
//    String fileName = String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
//
//    String thumbnailUrl = ftpConfig.getDefaultDocumentThumbnailUrl();
//
//    if (!file.isEmpty()) {
//      try {
//        byte[] thumbnailBytes = thumbnailGenerator.generateDocumentThumbnail(file);
//        String uploadedThumbnailUrl = ftpUtil.uploadThumbnailBytes(thumbnailBytes, fileName);
//        thumbnailUrl = uploadedThumbnailUrl;
//      } catch (IOException e) {
//        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
//      }
//    }
//
//    return TestDto.builder()
//        .fileUrl(thumbnailUrl)
//        .build();
//  }
//
//  /**
//   * 이미지 썸네일 저장 메서드
//   *
//   * @param imageFiles 업로드할 이미지 파일 리스트
//   * @return 업로드된 썸네일의 URL
//   */
//  public TestDto saveImagesThumbnail(List<MultipartFile> imageFiles) {
//    if (imageFiles == null || imageFiles.isEmpty()) {
//      throw new CustomException(ErrorCode.FILE_LIST_EMPTY);
//    }
//
//    MultipartFile targetImageFile = imageFiles.get(0);
//
//    // 파일 유효성 검사
//    validateFile(targetImageFile);
//
//    // 파일 이름 처리
//    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
//    String baseName = FileUtil.getBaseName(targetImageFile.getOriginalFilename());
//    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat();
//
//    String fileName = String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
//
//    String thumbnailUrl = ftpConfig.getDefaultImageThumbnailUrl();
//
//    if (!targetImageFile.isEmpty()) {
//      try {
//        byte[] thumbnailBytes = thumbnailGenerator.generateImageThumbnail(targetImageFile);
//        String uploadedThumbnailUrl = ftpUtil.uploadThumbnailBytes(thumbnailBytes, fileName);
//        thumbnailUrl = uploadedThumbnailUrl;
//      } catch (IOException e) {
//        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
//      }
//    }
//
//    return TestDto.builder()
//        .fileUrl(thumbnailUrl)
//        .build();
//  }
//
//  /**
//   * 동영상 썸네일 저장 메서드
//   *
//   * @param videoFile 업로드할 동영상 파일
//   * @return 업로드된 썸네일의 URL
//   */
//  public TestDto saveVideoThumbnail(MultipartFile videoFile) {
//    // 파일 유효성 검사
//    validateFile(videoFile);
//
//    // 파일 이름 처리
//    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
//    String baseName = FileUtil.getBaseName(videoFile.getOriginalFilename());
//    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat();
//
//    String fileName = String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
//
//    String thumbnailUrl = ftpConfig.getDefaultVideoThumbnailUrl();
//
//    if (!videoFile.isEmpty()) {
//      try {
//        byte[] thumbnailBytes = thumbnailGenerator.generateMediaThumbnail(videoFile);
//        String uploadedThumbnailUrl = ftpUtil.uploadThumbnailBytes(thumbnailBytes, fileName);
//        thumbnailUrl = uploadedThumbnailUrl;
//      } catch (IOException e) {
//        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
//      }
//    }
//
//    return TestDto.builder()
//        .fileUrl(thumbnailUrl)
//        .build();
//  }
//
//  /**
//   * 음원 썸네일 저장 메서드
//   *
//   * @param musicFile 업로드할 음원 파일
//   * @return 업로드된 썸네일의 URL
//   */
//  public TestDto saveMusicThumbnail(MultipartFile musicFile) {
//    // 파일 유효성 검사
//    validateFile(musicFile);
//
//    // 파일 이름 처리
//    String curTimeStr = TimeUtil.formatLocalDateTimeNowForFileName();
//    String baseName = FileUtil.getBaseName(musicFile.getOriginalFilename());
//    String thumbnailExtension = thumbnailGenerator.getOutputThumbnailFormat();
//
//    String fileName = String.format("%s_%s.%s", curTimeStr, baseName, thumbnailExtension);
//
//    String thumbnailUrl = ftpConfig.getDefaultMusicThumbnailUrl();
//
//    if (!musicFile.isEmpty()) {
//      try {
//        byte[] thumbnailBytes = thumbnailGenerator.generateMediaThumbnail(musicFile);
//        String uploadedThumbnailUrl = ftpUtil.uploadThumbnailBytes(thumbnailBytes, fileName);
//        thumbnailUrl = uploadedThumbnailUrl;
//      } catch (IOException e) {
//        throw new CustomException(ErrorCode.FTP_FILE_UPLOAD_ERROR);
//      }
//    }
//
//    return TestDto.builder()
//        .fileUrl(thumbnailUrl)
//        .build();
//  }
//
//  /**
//   * 파일 유효성 검사 메서드
//   *
//   * @param file 검사할 파일
//   */
//  private void validateFile(MultipartFile file) {
//    if (file == null || file.isEmpty()) {
//      throw new CustomException(ErrorCode.FILE_EMPTY);
//    }
//    if (file.getSize() > MAX_FILE_UPLOAD_SIZE * 1024 * 1024) {
//      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
//    }
//    try {
//      MimeType.fromString(file.getContentType());
//    } catch (CustomException e) {
//      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
//    }
//  }
//}
