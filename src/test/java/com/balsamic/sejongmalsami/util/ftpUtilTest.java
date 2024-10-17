package com.balsamic.sejongmalsami.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class FtpUtilTest {

  @Autowired
  private FtpUtil ftpUtil;

  private final String[] fileNames = {
      "테스트_문서_1.pdf",
      "테스트_이미지_1.png"
  };

  /**
   * 여러 파일을 FTP 서버에 업로드하는 테스트
   */
  @Test
  void testUploadFiles() {
    List<String> uploadedFtpFiles = new ArrayList<>();

    for (String fileName : fileNames) {
      try {
        // 파일 리소스 로드
        ClassPathResource resource = new ClassPathResource("document/" + fileName);
        if (!resource.exists()) {
          log.error("파일을 찾을 수 없습니다: {}", fileName);
          continue;
        }
        log.info("파일을 성공적으로 찾았습니다: {}", fileName);

        // MultipartFile 생성
        MultipartFile multipartFile = new MockMultipartFile(
            "file",
            fileName,
            Files.probeContentType(resource.getFile().toPath()),
            resource.getInputStream()
        );

        // FTP 업로드
        ftpUtil.uploadFile(multipartFile);
        uploadedFtpFiles.add(fileName);
        log.info("FTP 업로드 성공: {}", fileName);

      } catch (IOException e) {
        log.error("파일 읽기 중 예외 발생: {}", e.getMessage());
      } catch (Exception e) {
        log.error("FTP 업로드 중 예외 발생: {}", e.getMessage());
      }
    }

    // 업로드된 파일 목록 로깅
    if (!uploadedFtpFiles.isEmpty()) {
      log.info("업로드된 파일 목록: {}", uploadedFtpFiles);
    } else {
      log.warn("업로드된 파일이 없습니다.");
    }

//    // 업로드된 파일 삭제
//    for (String fileName : uploadedFtpFiles) {
//      try {
//        ftpUtil.deleteFile(fileName);
//        log.info("테스트 후 FTP 파일 삭제 성공: {}", fileName);
//      } catch (Exception e) {
//        log.error("테스트 후 FTP 파일 삭제 실패: {} - {}", fileName, e.getMessage());
//      }
//    }
  }

  /**
   * 업로드한 파일들을 FTP 서버에서 삭제하는 테스트
   */
  @Test
  void testDeleteFiles() {
    List<String> uploadedFtpFiles = new ArrayList<>();

    for (String fileName : fileNames) {
      try {
        // 파일 리소스 로드
        ClassPathResource resource = new ClassPathResource("document/" + fileName);
        if (!resource.exists()) {
          log.error("파일을 찾을 수 없습니다: {}", fileName);
          continue;
        }
        log.info("파일을 성공적으로 찾았습니다: {}", fileName);

        // MultipartFile 생성
        MultipartFile multipartFile = new MockMultipartFile(
            "file",
            fileName,
            Files.probeContentType(resource.getFile().toPath()),
            resource.getInputStream()
        );

        // FTP 업로드
        ftpUtil.uploadFile(multipartFile);
        uploadedFtpFiles.add(fileName);
        log.info("FTP 업로드 성공: {}", fileName);

      } catch (IOException e) {
        log.error("파일 읽기 중 예외 발생: {}", e.getMessage());
      } catch (Exception e) {
        log.error("FTP 업로드 중 예외 발생: {}", e.getMessage());
      }
    }

    if (uploadedFtpFiles.isEmpty()) {
      log.warn("삭제할 업로드된 파일이 없습니다.");
      return;
    }

    for (String fileName : uploadedFtpFiles) {
      try {
        ftpUtil.deleteFile(fileName);
        log.info("FTP 삭제 성공: {}", fileName);
      } catch (Exception e) {
        log.error("FTP 삭제 중 예외 발생: {}", e.getMessage());
      }
    }
  }
}
