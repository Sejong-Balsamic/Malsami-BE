//package com.balsamic.sejongmalsami.util;
//
//import java.awt.image.BufferedImage;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import javax.imageio.ImageIO;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.net.ftp.FTPClient;
//import org.apache.commons.pool2.impl.GenericObjectPool;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.List;
//
//@SpringBootTest
//@ActiveProfiles("dev")
//@Slf4j
//class FtpUtilTest {
//
//  @Autowired
//  private FtpUtil ftpUtil;
//
//  @Autowired
//  private ImageThumbnailGenerator imageThumbnailGenerator;
//
//  @Autowired
//  private GenericObjectPool<FTPClient> ftpClientPool;
//
//  private final String[] documentFileNames = {
//      "테스트_문서_PDF.pdf",
//      "테스트_이미지_JPEG.jpeg",
//      "테스트_이미지_JPG.jpg",
//      "테스트_이미지_PNG.png",
//      "테스트_이미지_WEBP.webp"
//  };
//
//  /**
//   * 메인 테스트 메소드
//   */
//  @Test
//  void mainTest() {
////    testUploadDocuments();
////    testDeleteDocuments();
////    testPoolLimit();
//    testUploadThumbnails();
////    testWebPConversion();
//  }
//
//  void testUploadDocuments() {
//    List<String> uploadedFtpFiles = new ArrayList<>();
//
//    for (String fileName : documentFileNames) {
//      try {
//        ClassPathResource resource = new ClassPathResource("document/" + fileName);
//        if (!resource.exists()) {
//          log.error("파일을 찾을 수 없습니다: {}", fileName);
//          continue;
//        }
//        log.info("파일을 성공적으로 찾았습니다: {}", fileName);
//
//        MultipartFile multipartFile = new MockMultipartFile(
//            "file",
//            fileName,
//            Files.probeContentType(resource.getFile().toPath()),
//            resource.getInputStream()
//        );
//
//        ftpUtil.uploadDocument(multipartFile);
//        uploadedFtpFiles.add(fileName);
//        log.info("FTP 문서 업로드 성공: {}", fileName);
//
//      } catch (IOException e) {
//        log.error("파일 읽기 중 예외 발생: {}", e.getMessage());
//      } catch (Exception e) {
//        log.error("FTP 업로드 중 예외 발생: {}", e.getMessage());
//      }
//    }
//
//    if (!uploadedFtpFiles.isEmpty()) {
//      log.info("업로드된 문서 파일 목록: {}", uploadedFtpFiles);
//    } else {
//      log.warn("업로드된 문서 파일이 없습니다.");
//    }
//  }
//
//  void testDeleteDocuments() {
//    List<String> uploadedFtpFiles = new ArrayList<>();
//
//    for (String fileName : documentFileNames) {
//      try {
//        ClassPathResource resource = new ClassPathResource("document/" + fileName);
//        if (!resource.exists()) {
//          log.error("파일을 찾을 수 없습니다: {}", fileName);
//          continue;
//        }
//        log.info("파일을 성공적으로 찾았습니다: {}", fileName);
//
//        MultipartFile multipartFile = new MockMultipartFile(
//            "file",
//            fileName,
//            Files.probeContentType(resource.getFile().toPath()),
//            resource.getInputStream()
//        );
//
//        ftpUtil.uploadDocument(multipartFile);
//        uploadedFtpFiles.add(fileName);
//        log.info("FTP 문서 업로드 성공: {}", fileName);
//
//      } catch (IOException e) {
//        log.error("파일 읽기 중 예외 발생: {}", e.getMessage());
//      } catch (Exception e) {
//        log.error("FTP 업로드 중 예외 발생: {}", e.getMessage());
//      }
//    }
//
//    if (uploadedFtpFiles.isEmpty()) {
//      log.warn("삭제할 업로드된 문서 파일이 없습니다.");
//      return;
//    }
//
//    for (String fileName : uploadedFtpFiles) {
//      try {
//        ftpUtil.deleteDocument(fileName);
//        log.info("FTP 문서 삭제 성공: {}", fileName);
//      } catch (Exception e) {
//        log.error("FTP 문서 삭제 중 예외 발생: {}", e.getMessage());
//      }
//    }
//  }
//
//  void testPoolLimit() {
//    int numberOfThreads = 15;
//    List<Thread> threads = new ArrayList<>();
//    List<String> uploadedFtpFiles = new ArrayList<>();
//
//    log.info("테스트 시작: {}개의 스레드를 사용하여 FTP 업로드 시도", numberOfThreads);
//    log.info("초기 풀 상태 - MaxTotal: {}, Active: {}, Idle: {}",
//        ftpClientPool.getMaxTotal(),
//        ftpClientPool.getNumActive(),
//        ftpClientPool.getNumIdle());
//
//    for (int i = 0; i < numberOfThreads; i++) {
//      String fileName = "test_file_" + i + ".txt";
//      MultipartFile multipartFile = new MockMultipartFile(
//          "file",
//          fileName,
//          "text/plain",
//          ("Content of file " + i).getBytes()
//      );
//
//      Thread thread = new Thread(() -> {
//        try {
//          log.info("Thread {} 시작: {}", Thread.currentThread().getName(), fileName);
//          ftpUtil.uploadDocument(multipartFile);
//          synchronized (uploadedFtpFiles) {
//            uploadedFtpFiles.add(fileName);
//          }
//          log.info("Thread {} 업로드 성공: {}", Thread.currentThread().getName(), fileName);
//        } catch (Exception e) {
//          log.error("Thread {} 업로드 실패: {} - {}", Thread.currentThread().getName(), fileName, e.getMessage());
//        }
//      }, "Upload-Thread-" + i);
//      threads.add(thread);
//    }
//
//    long startTime = System.currentTimeMillis();
//
//    for (Thread thread : threads) {
//      thread.start();
//    }
//
//    for (Thread thread : threads) {
//      try {
//        thread.join();
//      } catch (InterruptedException e) {
//        log.error("Thread join interrupted: {}", e.getMessage());
//        Thread.currentThread().interrupt();
//      }
//    }
//
//    long endTime = System.currentTimeMillis();
//    log.info("모든 스레드 완료. 소요 시간: {} ms", (endTime - startTime));
//
//    log.info("최종 풀 상태 - MaxTotal: {}, Active: {}, Idle: {}",
//        ftpClientPool.getMaxTotal(),
//        ftpClientPool.getNumActive(),
//        ftpClientPool.getNumIdle());
//
//    if (!uploadedFtpFiles.isEmpty()) {
//      log.info("업로드된 파일 목록: {}", uploadedFtpFiles);
//    } else {
//      log.warn("업로드된 파일이 없습니다.");
//    }
//  }
//
//  void testUploadThumbnails() {
//    List<String> uploadedThumbnailFiles = new ArrayList<>();
//
//    for (String fileName : documentFileNames) {
//      try {
//        ClassPathResource resource = new ClassPathResource("document/" + fileName);
//        if (!resource.exists()) {
//          log.error("파일을 찾을 수 없습니다: {}", fileName);
//          continue;
//        }
//        log.info("파일을 성공적으로 찾았습니다: {}", fileName);
//
//        MultipartFile multipartFile = new MockMultipartFile(
//            "file",
//            fileName,
//            Files.probeContentType(resource.getFile().toPath()),
//            resource.getInputStream()
//        );
//
//        byte[] thumbnail;
//        boolean isWebp = fileName.toLowerCase().endsWith(".webp");
//        if (fileName.toLowerCase().endsWith(".pdf")) {
//          thumbnail = imageThumbnailGenerator.generatePdfThumbnail(multipartFile.getInputStream());
//        } else {
//          thumbnail = imageThumbnailGenerator.generateImageThumbnail(multipartFile);
//        }
//
//        // WEBP 이미지의 경우 썸네일을 생성하지 않았으므로, 별도의 처리 필요
//        String thumbnailFilename;
//        if (isWebp) {
//          // 원본 파일명을 유지하거나 다른 형식으로 처리
//          thumbnailFilename = fileName; // 또는 다른 로직 적용
//          log.info("WebP 이미지의 경우 썸네일을 생성하지 않았으므로 원본 파일명을 사용합니다: {}", thumbnailFilename);
//        } else {
//          thumbnailFilename = fileName.replaceAll("\\.[^.]+$", "." + imageThumbnailGenerator.getOutputThumbnailFormat());
//        }
//
//        ftpUtil.uploadThumbnailBytes(thumbnail, thumbnailFilename);
//        uploadedThumbnailFiles.add(thumbnailFilename);
//        log.info("FTP 썸네일 업로드 성공: {}", thumbnailFilename);
//
//        if (!isWebp) {
//          BufferedImage thumbnailImage = ImageIO.read(new ByteArrayInputStream(thumbnail));
//          log.info("생성된 썸네일 이미지 크기: {}x{}", thumbnailImage.getWidth(), thumbnailImage.getHeight());
//        } else {
//          log.info("WebP 이미지의 경우 썸네일을 생성하지 않았습니다: {}", thumbnailFilename);
//        }
//
//      } catch (IOException e) {
//        log.error("썸네일 파일 처리 중 예외 발생: {}", e.getMessage());
//      } catch (Exception e) {
//        log.error("FTP 썸네일 업로드 중 예외 발생: {}", e.getMessage());
//      }
//    }
//
//    if (!uploadedThumbnailFiles.isEmpty()) {
//      log.info("업로드된 썸네일 파일 목록: {}", uploadedThumbnailFiles);
//    } else {
//      log.warn("업로드된 썸네일 파일이 없습니다.");
//    }
//  }
//
//
//  void testWebPConversion() {
//    try {
//      ClassPathResource resource = new ClassPathResource("document/테스트_이미지_PNG.png");
//      MultipartFile multipartFile = new MockMultipartFile(
//          "file",
//          "테스트_이미지_PNG.png",
//          Files.probeContentType(resource.getFile().toPath()),
//          resource.getInputStream()
//      );
//
//      // PNG를 WebP로 변환
//      byte[] webpImage = imageThumbnailGenerator.generateImageThumbnail(multipartFile);
//
//      // WebP 이미지 검증
//      BufferedImage convertedImage = ImageIO.read(new ByteArrayInputStream(webpImage));
//      log.info("PNG에서 변환된 WebP 이미지 크기: {}x{}", convertedImage.getWidth(), convertedImage.getHeight());
//
//      // 변환된 WebP 이미지를 파일로 저장 (테스트용)
//      String webpFilename = "converted_test_image.webp";
//      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//      ImageIO.write(convertedImage, "webp", outputStream);
//      ftpUtil.uploadThumbnailBytes(outputStream.toByteArray(), webpFilename);
//
//      log.info("WebP 변환 테스트 완료: {}", webpFilename);
//
//    } catch (IOException e) {
//      log.error("WebP 변환 테스트 중 예외 발생: {}", e.getMessage());
//    }
//  }
//}
