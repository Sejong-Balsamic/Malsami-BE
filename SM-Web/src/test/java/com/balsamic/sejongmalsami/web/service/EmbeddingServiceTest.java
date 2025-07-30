package com.balsamic.sejongmalsami.web.service;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.PostEmbedding;
import com.balsamic.sejongmalsami.post.service.PostEmbeddingService;
import com.balsamic.sejongmalsami.repository.postgres.PostEmbeddingRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
  @Autowired
  private PostEmbeddingRepository postEmbeddingRepository;

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
    postEmbeddingService.saveEmbedding(postId, text, contentType);
    PostEmbedding postEmbedding = postEmbeddingRepository.findByPostId(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.EMBEDDING_GENERATION_FAILED));
    LogUtil.superLog(postEmbedding);
    log.info("Embedding 테스트 완료 - Post ID: {}", postId);
  }
}
