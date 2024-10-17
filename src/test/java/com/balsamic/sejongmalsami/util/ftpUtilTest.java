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

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class FtpUtilTest {

  @Autowired
  private FtpUtil ftpUtil;

  private final List<String> uploadedFtpFiles = new ArrayList<>();

  @Test
  void testUploadFiles() throws Exception {

    String[] fileNames = {
        "테스트_문서_1.pdf",
        "테스트_이미지_1.png"
    };

    ClassLoader classLoader = getClass().getClassLoader();

    for (String fileName : fileNames) {
      URL resource = classLoader.getResource("document/" + fileName);
      if (resource == null) {
        log.error("파일을 찾을 수 없습니다: {}", fileName);
        continue; // 다음 파일로 넘어갑니다.
      } else {
        log.info("파일을 성공적으로 찾았습니다: {}", fileName);
      }

      File file = Paths.get(resource.toURI()).toFile();
      if (!file.exists()) {
        log.error("로컬 파일이 존재하지 않습니다: {}", file.getAbsolutePath());
        continue;
      } else {
        log.info("로컬 파일이 존재합니다: {}", file.getAbsolutePath());
      }

      log.info("로컬 파일 경로: {}", file.getAbsolutePath());

      // FTP 업로드
      try {
        ftpUtil.uploadFile(file);
        uploadedFtpFiles.add(file.getName());
        log.info("FTP 업로드 성공: {}", file.getName());
      } catch (Exception e) {
        log.error("FTP 업로드 실패: {} - {}", fileName, e.getMessage());
      }
    }
  }

//  @AfterEach
//  void cleanup() throws Exception {
//    for (String fileName : uploadedFtpFiles) {
//      try {
//        ftpUtil.deleteFile(fileName);
//        log.info("FTP 파일 삭제 성공: {}", fileName);
//      } catch (Exception e) {
//        log.error("FTP 파일 삭제 실패: {} - {}", fileName, e.getMessage());
//      }
//    }
//    uploadedFtpFiles.clear();
//  }
}
