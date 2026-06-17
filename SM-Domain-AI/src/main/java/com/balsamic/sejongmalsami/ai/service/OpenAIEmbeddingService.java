package com.balsamic.sejongmalsami.ai.service;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.properties.GoogleGenAiProperties;
import com.google.genai.Client;
import com.google.genai.errors.ClientException;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OpenAIEmbeddingService {

  private final Client genAiClient;
  private final GoogleGenAiProperties googleGenAiProperties;

  // genAiClient 는 @Lazy 로 주입해 실제 임베딩 호출 시점에만 초기화되도록 한다.
  // (google.genai.* 설정이 없어도 앱 기동은 영향을 받지 않는다)
  // 필드 @Lazy 는 Lombok 생성자로 전파되지 않으므로 명시적 생성자에서 직접 지정한다.
  public OpenAIEmbeddingService(@Lazy Client genAiClient,
                                GoogleGenAiProperties googleGenAiProperties) {
    this.genAiClient = genAiClient;
    this.googleGenAiProperties = googleGenAiProperties;
  }

  @Transactional
  public float[] generateEmbedding(String text) {
    return extractVector(fetchVertexAI(text));
  }

  /**
   * Vertex AI에 실제로 임베딩 요청을 보내는 로직
   *
   * @param text 임베딩을 생성할 텍스트
   * @return EmbedContentResponse
   */
  private EmbedContentResponse fetchVertexAI(String text) {
    try {
      return genAiClient.models.embedContent(
          googleGenAiProperties.getModel(),
          text,
          EmbedContentConfig.builder().build()
      );
    } catch (ClientException e) {
      log.error("Vertex AI 임베딩 API 호출 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.OPENAI_CONNECTION_ERROR);
    }
  }

  /**
   * 응답에서 벡터 값만 추출해 float[] 로 변환
   *
   * @param response EmbedContentResponse 객체
   * @return 임베딩 float[]
   */
  private float[] extractVector(EmbedContentResponse response) {
    List<Float> embeddingValues = response.embeddings()
        .flatMap(list -> list.stream().findFirst())
        .flatMap(ContentEmbedding::values)
        .orElseThrow(() -> new CustomException(ErrorCode.EMBEDDING_GENERATION_FAILED));
    int size = embeddingValues.size();
    float[] vector = new float[size];
    for (int i = 0; i < size; i++) {
      vector[i] = embeddingValues.get(i);
    }
    return vector;
  }
}
