package com.balsamic.sejongmalsami.ai.service;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.properties.VertexAiProperties;
import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Vertex AI 임베딩 클라이언트 구현체 (fallback 용)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VertexAiClientImpl implements VertexAiClient {

  private final Client embeddingClient;
  private final VertexAiProperties vertexAiProperties;

  @Override
  public EmbedContentResponse generateEmbedding(String text) {
    try {
      return embeddingClient.models.embedContent(
          vertexAiProperties.getEmbeddingModel(),
          text,
          EmbedContentConfig.builder().build()
      );
    } catch (ClientException e) {
      log.error("Vertex AI 임베딩 생성 실패: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.VERTEX_API_CALL_FAILED);
    }
  }
}
