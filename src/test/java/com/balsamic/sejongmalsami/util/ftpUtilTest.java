package com.balsamic.sejongmalsami.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
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

  @Autowired
  private GenericObjectPool<FTPClient> ftpClientPool;

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

  /**
   * 풀의 동작을 테스트하기 위한 동시 업로드 테스트
   * 최대 풀 크기(10)를 초과하는 15개의 동시 업로드 작업을 수행합니다.
   * 각 스레드의 시작과 종료 시점을 로깅하여 풀의 상태를 모니터링합니다.
   */
  @Test
  void testPoolLimit() {
    int numberOfThreads = 15;
    List<Thread> threads = new ArrayList<>();
    List<String> uploadedFtpFiles = new ArrayList<>();

    log.info("테스트 시작: {}개의 스레드를 사용하여 FTP 업로드 시도", numberOfThreads);
    log.info("초기 풀 상태 - MaxTotal: {}, Active: {}, Idle: {}",
        ftpClientPool.getMaxTotal(),
        ftpClientPool.getNumActive(),
        ftpClientPool.getNumIdle());

    for (int i = 0; i < numberOfThreads; i++) {
      String fileName = "test_file_" + i + ".txt";
      MultipartFile multipartFile = new MockMultipartFile(
          "file",
          fileName,
          "text/plain",
          ("Content of file " + i).getBytes()
      );

      Thread thread = new Thread(() -> {
        try {
          log.info("Thread {} 시작: {}", Thread.currentThread().getName(), fileName);
          ftpUtil.uploadFile(multipartFile);
          uploadedFtpFiles.add(fileName);
          log.info("Thread {} 업로드 성공: {}", Thread.currentThread().getName(), fileName);
        } catch (Exception e) {
          log.error("Thread {} 업로드 실패: {} - {}", Thread.currentThread().getName(), fileName, e.getMessage());
        }
      }, "Upload-Thread-" + i);
      threads.add(thread);
    }

    long startTime = System.currentTimeMillis();

    // 모든 스레드 시작
    for (Thread thread : threads) {
      thread.start();
    }

    // 모든 스레드가 완료될 때까지 대기
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        log.error("Thread join interrupted: {}", e.getMessage());
      }
    }

    long endTime = System.currentTimeMillis();
    log.info("모든 스레드 완료. 소요 시간: {} ms", (endTime - startTime));

    // 최종 풀 상태 로깅
    log.info("최종 풀 상태 - MaxTotal: {}, Active: {}, Idle: {}",
        ftpClientPool.getMaxTotal(),
        ftpClientPool.getNumActive(),
        ftpClientPool.getNumIdle());

    // 업로드된 파일 목록 로깅
    if (!uploadedFtpFiles.isEmpty()) {
      log.info("업로드된 파일 목록: {}", uploadedFtpFiles);
    } else {
      log.warn("업로드된 파일이 없습니다.");
    }

    // 업로드된 파일 삭제
    for (String fileName : uploadedFtpFiles) {
      try {
        ftpUtil.deleteFile(fileName);
        log.info("테스트 후 FTP 파일 삭제 성공: {}", fileName);
      } catch (Exception e) {
        log.error("테스트 후 FTP 파일 삭제 실패: {} - {}", fileName, e.getMessage());
      }
    }
  }


}
