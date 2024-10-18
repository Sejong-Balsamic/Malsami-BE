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

  private final String[] documentFileNames = {
      "테스트_문서_1.pdf",
      "테스트_이미지_1.png"
  };

  private final String[] thumbnailFileNames = {
      "테스트_썸네일_1.jpg"
  };

  /**
   * 메인 테스트 메소드
   */
  @Test
  void mainTest() {
//    testUploadDocuments();
//    testDeleteDocuments();
//    testPoolLimit();
    testUploadThumbnails();
//    testDeleteThumbnails();
  }

  void testUploadDocuments() {
    List<String> uploadedFtpFiles = new ArrayList<>();

    for (String fileName : documentFileNames) {
      try {
        ClassPathResource resource = new ClassPathResource("document/" + fileName);
        if (!resource.exists()) {
          log.error("파일을 찾을 수 없습니다: {}", fileName);
          continue;
        }
        log.info("파일을 성공적으로 찾았습니다: {}", fileName);

        MultipartFile multipartFile = new MockMultipartFile(
            "file",
            fileName,
            Files.probeContentType(resource.getFile().toPath()),
            resource.getInputStream()
        );

        ftpUtil.uploadDocument(multipartFile);
        uploadedFtpFiles.add(fileName);
        log.info("FTP 문서 업로드 성공: {}", fileName);

      } catch (IOException e) {
        log.error("파일 읽기 중 예외 발생: {}", e.getMessage());
      } catch (Exception e) {
        log.error("FTP 업로드 중 예외 발생: {}", e.getMessage());
      }
    }

    if (!uploadedFtpFiles.isEmpty()) {
      log.info("업로드된 문서 파일 목록: {}", uploadedFtpFiles);
    } else {
      log.warn("업로드된 문서 파일이 없습니다.");
    }
  }

  void testDeleteDocuments() {
    List<String> uploadedFtpFiles = new ArrayList<>();

    for (String fileName : documentFileNames) {
      try {
        ClassPathResource resource = new ClassPathResource("document/" + fileName);
        if (!resource.exists()) {
          log.error("파일을 찾을 수 없습니다: {}", fileName);
          continue;
        }
        log.info("파일을 성공적으로 찾았습니다: {}", fileName);

        MultipartFile multipartFile = new MockMultipartFile(
            "file",
            fileName,
            Files.probeContentType(resource.getFile().toPath()),
            resource.getInputStream()
        );

        ftpUtil.uploadDocument(multipartFile);
        uploadedFtpFiles.add(fileName);
        log.info("FTP 문서 업로드 성공: {}", fileName);

      } catch (IOException e) {
        log.error("파일 읽기 중 예외 발생: {}", e.getMessage());
      } catch (Exception e) {
        log.error("FTP 업로드 중 예외 발생: {}", e.getMessage());
      }
    }

    if (uploadedFtpFiles.isEmpty()) {
      log.warn("삭제할 업로드된 문서 파일이 없습니다.");
      return;
    }

    for (String fileName : uploadedFtpFiles) {
      try {
        ftpUtil.deleteDocument(fileName);
        log.info("FTP 문서 삭제 성공: {}", fileName);
      } catch (Exception e) {
        log.error("FTP 문서 삭제 중 예외 발생: {}", e.getMessage());
      }
    }
  }

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
          ftpUtil.uploadDocument(multipartFile);
          synchronized (uploadedFtpFiles) {
            uploadedFtpFiles.add(fileName);
          }
          log.info("Thread {} 업로드 성공: {}", Thread.currentThread().getName(), fileName);
        } catch (Exception e) {
          log.error("Thread {} 업로드 실패: {} - {}", Thread.currentThread().getName(), fileName, e.getMessage());
        }
      }, "Upload-Thread-" + i);
      threads.add(thread);
    }

    long startTime = System.currentTimeMillis();

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        log.error("Thread join interrupted: {}", e.getMessage());
        Thread.currentThread().interrupt();
      }
    }

    long endTime = System.currentTimeMillis();
    log.info("모든 스레드 완료. 소요 시간: {} ms", (endTime - startTime));

    log.info("최종 풀 상태 - MaxTotal: {}, Active: {}, Idle: {}",
        ftpClientPool.getMaxTotal(),
        ftpClientPool.getNumActive(),
        ftpClientPool.getNumIdle());

    if (!uploadedFtpFiles.isEmpty()) {
      log.info("업로드된 파일 목록: {}", uploadedFtpFiles);
    } else {
      log.warn("업로드된 파일이 없습니다.");
    }
  }

  void testUploadThumbnails() {
    List<String> uploadedThumbnailFiles = new ArrayList<>();

    for (String fileName : thumbnailFileNames) {
      try {
        ClassPathResource resource = new ClassPathResource("thumbnail/" + fileName);
        if (!resource.exists()) {
          log.error("썸네일 파일을 찾을 수 없습니다: {}", fileName);
          continue;
        }
        log.info("썸네일 파일을 성공적으로 찾았습니다: {}", fileName);

        MultipartFile multipartFile = new MockMultipartFile(
            "file",
            fileName,
            Files.probeContentType(resource.getFile().toPath()),
            resource.getInputStream()
        );

        ftpUtil.uploadThumbnail(multipartFile);
        uploadedThumbnailFiles.add(fileName);
        log.info("FTP 썸네일 업로드 성공: {}", fileName);

      } catch (IOException e) {
        log.error("썸네일 파일 읽기 중 예외 발생: {}", e.getMessage());
      } catch (Exception e) {
        log.error("FTP 썸네일 업로드 중 예외 발생: {}", e.getMessage());
      }
    }

    if (!uploadedThumbnailFiles.isEmpty()) {
      log.info("업로드된 썸네일 파일 목록: {}", uploadedThumbnailFiles);
    } else {
      log.warn("업로드된 썸네일 파일이 없습니다.");
    }
  }

  void testDeleteThumbnails() {
    List<String> uploadedThumbnailFiles = new ArrayList<>();

    for (String fileName : thumbnailFileNames) {
      try {
        ClassPathResource resource = new ClassPathResource("thumbnail/" + fileName);
        if (!resource.exists()) {
          log.error("썸네일 파일을 찾을 수 없습니다: {}", fileName);
          continue;
        }
        log.info("썸네일 파일을 성공적으로 찾았습니다: {}", fileName);

        MultipartFile multipartFile = new MockMultipartFile(
            "file",
            fileName,
            Files.probeContentType(resource.getFile().toPath()),
            resource.getInputStream()
        );

        ftpUtil.uploadThumbnail(multipartFile);
        uploadedThumbnailFiles.add(fileName);
        log.info("FTP 썸네일 업로드 성공: {}", fileName);

      } catch (IOException e) {
        log.error("썸네일 파일 읽기 중 예외 발생: {}", e.getMessage());
      } catch (Exception e) {
        log.error("FTP 썸네일 업로드 중 예외 발생: {}", e.getMessage());
      }
    }

    if (uploadedThumbnailFiles.isEmpty()) {
      log.warn("삭제할 업로드된 썸네일 파일이 없습니다.");
      return;
    }

    for (String fileName : uploadedThumbnailFiles) {
      try {
        ftpUtil.deleteThumbnail(fileName);
        log.info("FTP 썸네일 삭제 성공: {}", fileName);
      } catch (Exception e) {
        log.error("FTP 썸네일 삭제 중 예외 발생: {}", e.getMessage());
      }
    }
  }
}
