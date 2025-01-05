package com.balsamic.sejongmalsami.service;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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
      // 요청 데이터 생성
      Map<String, Object> requestData = new HashMap<>();
      requestData.put("model", "text-embedding-ada-002");
      requestData.put("input", inputText);
      String requestBody = gson.toJson(requestData);

      // HTTP 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(apiKey);

      // 요청 HttpEntity 생성
      HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

      // RestTemplate 요청 실행
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<String> response = restTemplate.exchange(
          apiUrl, HttpMethod.POST, entity, String.class
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
    } catch (HttpClientErrorException e) {
      log.error("OpenAI API 요청 오류 - 상태 코드: {}, 메시지: {}", e.getStatusCode(), e.getResponseBodyAsString());
      HttpStatusCode statusCode = e.getStatusCode();
      if (statusCode.equals(UNAUTHORIZED)) {
        throw new CustomException(ErrorCode.OPENAI_AUTHENTICATION_ERROR);
      } else if (statusCode.equals(FORBIDDEN)) {
        throw new CustomException(ErrorCode.OPENAI_PERMISSION_DENIED);
      } else if (statusCode.equals(BAD_REQUEST)) {
        throw new CustomException(ErrorCode.OPENAI_INVALID_REQUEST_ERROR);
      } else if (statusCode.equals(NOT_FOUND)) {
        throw new CustomException(ErrorCode.OPENAI_RESOURCE_NOT_FOUND);
      } else if (statusCode.equals(TOO_MANY_REQUESTS)) {
        throw new CustomException(ErrorCode.OPENAI_RATE_LIMIT_EXCEEDED);
      }
      throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
    } catch (HttpServerErrorException e) {
      log.error("OpenAI 서버 오류 - 상태 코드: {}, 메시지: {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new CustomException(ErrorCode.OPENAI_SERVER_ERROR);
    } catch (ResourceAccessException e) {
      log.error("OpenAI 서비스 연결 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.OPENAI_CONNECTION_ERROR);
    } catch (Exception e) {
      log.error("Embedding 생성 중 예기치 못한 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.OPENAI_INVALID_RESPONSE);
    }
  }
}
