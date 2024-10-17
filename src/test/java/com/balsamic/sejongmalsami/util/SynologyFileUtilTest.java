package com.balsamic.sejongmalsami.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class SynologyFileUtilTest {

  @Autowired
  private FtpService ftpService;

  private final List<String> uploadedFtpFiles = new ArrayList<>();

  @Test
  void testUploadFiles() throws Exception {
    String ftpRemotePath = "/projects/sejong-malsami/document"; // FTP 업로드 경로

    String[] fileNames = {
        "테스트_문서_1.pdf",
        "테스트_이미지_1.png"
    };

    ClassLoader classLoader = getClass().getClassLoader();

    for (String fileName : fileNames) {
      URL resource = classLoader.getResource("document/" + fileName);
      assertNotNull(resource, "파일을 찾을 수 없습니다: " + fileName);

      File file = Paths.get(resource.toURI()).toFile();
      assertTrue(file.exists(), "로컬 파일이 존재하지 않습니다: " + file.getAbsolutePath());

      log.info("로컬 파일 경로: {}", file.getAbsolutePath());

      // FTP 업로드 시도
      try {
        ftpService.uploadFile(ftpRemotePath, file);
        String ftpUploadedFilePath = ftpRemotePath + "/" + file.getName();
        uploadedFtpFiles.add(ftpUploadedFilePath);
        log.info("FTP 업로드 성공: {}", ftpUploadedFilePath);
      } catch (Exception e) {
        fail("FTP 업로드 실패: " + fileName + " - " + e.getMessage());
      }
    }
  }

//  @AfterEach
//  void cleanup() throws Exception {
//    for (String fileName : uploadedFtpFiles) {
//      try {
//        ftpService.deleteFile(fileName);
//        log.info("FTP 파일 삭제 성공: {}", fileName);
//      } catch (Exception e) {
//        log.error("FTP 파일 삭제 실패: {} - {}", fileName, e.getMessage());
//      }
//    }
//    uploadedFtpFiles.clear();
//  }
}
