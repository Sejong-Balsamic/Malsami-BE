package com.balsamic.sejongmalsami.service;

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

  public Page<PostEmbedding> searchSimilarEmbeddings(
      float[] queryVector,
      float threshold,
      ContentType contentType,
      Integer pageSize,
      Integer pageNumber) {
    log.info("유사 Embedding 검색 시작 - threshold: {}, pageSize: {}, pageNumber: {}", threshold, pageSize, pageNumber);
    try {
      Pageable pageable = PageRequest.of(pageNumber, pageSize);
      Page<PostEmbedding> similarEmbeddings = postEmbeddingRepository.findSimilarEmbeddings(queryVector, threshold, contentType.name(), pageable);
      log.info("유사 Embedding 검색 완료 - 총 결과 수: {}, 현재 페이지: {}, 총 페이지: {}", similarEmbeddings.getTotalElements(), similarEmbeddings.getNumber(), similarEmbeddings.getTotalPages());
      return similarEmbeddings;
    } catch (Exception e) {
      log.error("유사 Embedding 검색 중 오류 발생 - 오류: {}", e.getMessage(), e);
      throw new RuntimeException("유사 Embedding 검색 중 오류 발생");
    }
  }
}
