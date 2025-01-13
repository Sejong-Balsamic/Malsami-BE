package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.util.log.LogUtil;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class EmbeddingServiceTest {
  @Autowired
  private PostEmbeddingService postEmbeddingService;

  @Test
  public void mainTest() {
    // Test 하기전 saveEmbedding 메소드 Async 주석 처리
    LogUtil.timeLog(this::testEmbeddingService);
  }

  public void testEmbeddingService() {
    UUID postId = UUID.randomUUID();
    String text = "Sample text for embedding generation.";
    ContentType contentType = ContentType.QUESTION;

    log.info("Embedding 테스트 시작 - Post ID: {}", postId);
//    PostEmbedding postEmbedding = postEmbeddingService.saveEmbedding(postId, text, contentType);
//    LogUtil.superLog(postEmbedding);
    log.info("Embedding 테스트 완료 - Post ID: {}", postId);
  }
}
