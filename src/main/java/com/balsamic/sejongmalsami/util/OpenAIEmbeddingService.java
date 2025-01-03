package com.balsamic.sejongmalsami.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OpenAIEmbeddingService {

  private final String apiUrl = "https://api.openai.com/v1/embeddings";

  @Value("${openai.embedding.api-key}")
  private String apiKey;

  public float[] generateEmbedding(String inputText) {
    Gson gson = new Gson();
    log.info("텍스트를 Embedding으로 변환 중 - Text: {}", inputText);
    try {
      Map<String, Object> requestData = new HashMap<>();
      requestData.put("model", "text-embedding-ada-002");
      requestData.put("input", inputText);
      // 요청 본문 작성: Map -> JSON String 변환
      String requestBody = gson.toJson(requestData);

      // HTTP 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);

      // 요청 HttpEntity 생성
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response = restTemplate.exchange(
          apiUrl,
          HttpMethod.POST,
          entity,
          String.class
      );

      // 응답 데이터 파싱
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());
      JsonNode embeddingNode = root.path("data").get(0).path("embedding");

      // JSON 배열 -> float[] 변환
      float[] embedding = new float[embeddingNode.size()];
      for (int i = 0; i < embeddingNode.size(); i++) {
        embedding[i] = (float) embeddingNode.get(i).asDouble();
      }
      return embedding;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error while generating embedding");
    }
  }
}
