package com.balsamic.sejongmalsami.ai.service;

import com.google.genai.types.EmbedContentResponse;

/**
 * Vertex AI 임베딩 클라이언트 (SUH-AIder 실패 시 fallback 용)
 */
public interface VertexAiClient {

  /**
   * 텍스트를 Vertex AI 임베딩 모델로 임베딩한다.
   *
   * @param text 임베딩할 텍스트
   * @return Vertex AI 임베딩 응답
   */
  EmbedContentResponse generateEmbedding(String text);
}
