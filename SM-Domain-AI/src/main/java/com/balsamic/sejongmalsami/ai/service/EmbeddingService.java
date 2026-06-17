package com.balsamic.sejongmalsami.ai.service;

import com.balsamic.sejongmalsami.ai.EmbeddingUtil;
import com.balsamic.sejongmalsami.util.CommonUtil;
import com.balsamic.sejongmalsami.util.properties.SuhAiderProperties;
import com.google.genai.types.EmbedContentResponse;
import java.util.List;
import kr.suhsaechan.ai.service.SuhAiderEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 임베딩 생성 서비스
 *
 * <p>SUH-AIder(자체 AI 서버)를 메인 엔진으로 사용하고, 실패 시 Vertex AI 로 fallback 한다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddingService {

  private final SuhAiderEngine suhAiderEngine;
  private final SuhAiderProperties suhAiderProperties;
  private final VertexAiClient vertexAiClient;

  /**
   * 텍스트를 정규화한 뒤 임베딩 벡터를 생성한다.
   * SUH-AIder 우선, 실패 시 Vertex AI fallback.
   *
   * @param text 임베딩할 원본 텍스트
   * @return 임베딩 벡터 float[]
   */
  public float[] generateEmbedding(String text) {
    String normalized = CommonUtil.normalizeSpaces(text);
    long startMs = System.currentTimeMillis();

    // 1순위: SUH-AIder
    try {
      String embeddingModel = suhAiderProperties.getEmbedding().getDefaultModel();
      List<Double> embedding = suhAiderEngine.embed(embeddingModel, normalized);
      float[] vector = CommonUtil.convertDoubleListToFloatArray(embedding);
      log.debug("SUH-AIder 임베딩 생성 완료: 차원={}, 지연시간={}ms",
          vector.length, System.currentTimeMillis() - startMs);
      return vector;
    } catch (Exception e) {
      log.warn("SUH-AIder 임베딩 실패, Vertex AI fallback 시도: {}", e.getMessage());
    }

    // fallback: Vertex AI
    EmbedContentResponse response = vertexAiClient.generateEmbedding(normalized);
    float[] vector = EmbeddingUtil.extractVector(response);
    log.debug("Vertex AI fallback 임베딩 완료: 차원={}, 지연시간={}ms",
        vector.length, System.currentTimeMillis() - startMs);
    return vector;
  }
}
