package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.EmbeddingCommand;
import com.balsamic.sejongmalsami.object.EmbeddingDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.PostEmbedding;
import com.balsamic.sejongmalsami.repository.postgres.PostEmbeddingRepository;
import com.balsamic.sejongmalsami.util.OpenAIEmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostEmbeddingService {

  private final OpenAIEmbeddingService embeddingService;
  private final PostEmbeddingRepository postEmbeddingRepository;
  private final ObjectMapper objectMapper;

  @Async
  public PostEmbedding saveEmbedding(UUID postId, String text, ContentType contentType) {
    log.info("Embedding 저장 시작 - Post ID: {}, ContentType: {}", postId, contentType);
    try {
      // Embedding 벡터 생성
      float[] embeddingArray = embeddingService.generateEmbedding(text);
      log.info("Embedding 생성 완료 - Post ID: {}", postId);

      // JSON 문자열로 변환
      String embeddingJson = objectMapper.writeValueAsString(embeddingArray);
      log.debug("Embedding JSON 변환 완료 - Post ID: {}", postId);

      PostEmbedding postEmbedding = PostEmbedding.builder()
          .postId(postId)
          .embedding(embeddingJson)
          .contentType(contentType)
          .build();

      PostEmbedding savedPostEmbedding = postEmbeddingRepository.save(postEmbedding);
      log.info("Embedding 저장 완료 - Post ID: {}", postId);
      return savedPostEmbedding;
    } catch (Exception e) {
      log.error("Embedding 저장 중 오류 발생 - Post ID: {}, 오류: {}", postId, e.getMessage(), e);
      throw new RuntimeException("Embedding 저장 중 오류 발생");
    }
  }

  public EmbeddingDto searchSimilarEmbeddingsByText(EmbeddingCommand command) {
    log.info("Embedding 검색 시작 - Text: {}, threshold: {}", command.getText(), command.getThreshold());
    try {
      // 검색 텍스트 -> Embedding 변환
      float[] queryVector = embeddingService.generateEmbedding(command.getText());

      // 검색
      Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize());
      Page<PostEmbedding> postEmbeddingsPage = postEmbeddingRepository.findSimilarEmbeddings(
          queryVector,
          command.getThreshold(),
          command.getContentType().name(),
          pageable
      );
      log.info("Embedding 검색 완료 - 총 결과 수: {}", postEmbeddingsPage.getTotalElements());
      return EmbeddingDto.builder()
          .postEmbeddingsPage(postEmbeddingsPage)
          .build();
    } catch (Exception e) {
      log.error("Embedding 검색 중 오류 발생 - 오류: {}", e.getMessage(), e);
      throw new RuntimeException("Embedding 검색 중 오류 발생");
    }
  }
}
