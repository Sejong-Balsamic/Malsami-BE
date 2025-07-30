package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.log.LogUtil.lineLog;
import static com.balsamic.sejongmalsami.util.log.LogUtil.timeLog;

import com.balsamic.sejongmalsami.application.test.TestDataGenerator;
import com.balsamic.sejongmalsami.postgres.DocumentFile;
import com.balsamic.sejongmalsami.postgres.DocumentPost;
import com.balsamic.sejongmalsami.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
@Transactional
class DocumentFileServiceTest {

  @Autowired
  DocumentFileService documentFileService;

  @Autowired
  TestDataGenerator testDataGenerator;

  @Autowired
  DocumentFileRepository documentFileRepository;

  @Qualifier("applicationTaskExecutor")
  @Autowired
  TaskExecutor taskExecutor;

  private List<DocumentFile> documentFiles = new ArrayList<>();

  private Member member;


  @BeforeEach
  public void init() {

    // Mock 회원 정의
    Member member = testDataGenerator.createMockMember();
    this.member = member;

    // Mock DocumentPostFile 정의
    DocumentPost documentPost = testDataGenerator.createMockDocumentPost(member);
    for (int i = 0; i < 20; i++) {
      this.documentFiles.add(testDataGenerator.createMockDocumentFile(member, documentPost));
    }

  }

  @Test
  public void mainTest() {
    lineLog("updateIsDownloadedDocumentFiles_동기적");
    timeLog(this::updateIsDownloadedDocumentFiles_동기적);

    lineLog("updateIsDownloadedDocumentFiles_멀티스레드");
    timeLog(this::updateIsDownloadedDocumentFiles_멀티스레드);
    lineLog(null);
  }

  public void updateIsDownloadedDocumentFiles_동기적() {
    log.info("[동기적] 작업 시작. 전체 파일 수: {}", documentFiles.size());
    for (DocumentFile documentFile : documentFiles) {
      log.debug("[동기적] 파일 처리 시작. File ID: {}", documentFile.getDocumentFileId());
      boolean exists = documentFileRepository.existsByUploader(member);
      documentFile.setIsDownloaded(exists);
      log.debug("[동기적] 파일 처리 완료. File ID: {}, IsDownloaded: {}", documentFile.getDocumentFileId(), exists);
    }
    log.info("[동기적] 작업 완료.");
  }

  public void updateIsDownloadedDocumentFiles_멀티스레드() {
    log.info("[멀티스레드] 작업 시작. 전체 파일 수: {}", documentFiles.size());

    // CompletableFuture 리스트 생성
    List<CompletableFuture<Void>> futures = new ArrayList<>();

    for (DocumentFile documentFile : documentFiles) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        log.debug("[멀티스레드] 파일 처리 시작. File ID: {}", documentFile.getDocumentFileId());
        boolean exists = documentFileRepository.existsByUploader(member);
        documentFile.setIsDownloaded(exists);
        log.debug("[멀티스레드] 파일 처리 완료. File ID: {}, IsDownloaded: {}", documentFile.getDocumentFileId(), exists);
      }, taskExecutor);

      futures.add(future);
    }

    // 모든 CompletableFuture 완료 대기
    futures.forEach(CompletableFuture::join);

    log.info("[멀티스레드] 작업 완료.");
  }
}