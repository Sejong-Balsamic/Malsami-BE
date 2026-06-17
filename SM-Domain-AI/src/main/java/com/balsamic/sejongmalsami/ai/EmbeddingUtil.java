package com.balsamic.sejongmalsami.ai;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.google.genai.types.ContentEmbedding;
import com.google.genai.types.EmbedContentResponse;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;

/**
 * 임베딩 벡터 변환 유틸리티
 */
@UtilityClass
public class EmbeddingUtil {

  /**
   * 임베딩 벡터를 PostgreSQL vector 리터럴 문자열로 변환 (예: "[0.1,0.2,...]")
   */
  public static String toVectorLiteral(float[] embedding) {
    return IntStream.range(0, embedding.length)
        .mapToObj(i -> Float.toString(embedding[i]))
        .collect(Collectors.joining(",", "[", "]"));
  }

  /**
   * Vertex AI 응답에서 벡터 값만 추출해 float[] 로 변환
   *
   * @param response EmbedContentResponse 객체
   * @return 임베딩 float[]
   */
  public static float[] extractVector(EmbedContentResponse response) {
    List<Float> embeddingValues = response.embeddings()
        .flatMap(list -> list.stream().findFirst())
        .flatMap(ContentEmbedding::values)
        .orElseThrow(() -> new CustomException(ErrorCode.EMBEDDING_NOT_FOUND));
    int size = embeddingValues.size();

    float[] vector = new float[size];
    for (int i = 0; i < size; i++) {
      vector[i] = embeddingValues.get(i);
    }
    return vector;
  }
}
