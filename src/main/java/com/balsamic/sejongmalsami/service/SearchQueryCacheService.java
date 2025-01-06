package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.SearchQueryCache;
import com.balsamic.sejongmalsami.repository.postgres.SearchQueryCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchQueryCacheService {

  private final SearchQueryCacheRepository searchQueryCacheRepository;
  private final OpenAIEmbeddingService openAIEmbeddingService;

  /**
   * 동일 검색어에 대한 Embedding 캐싱 처리 DB에 존재한다면 해당 검색어 Embedding값 반환 DB에 없는 경우 OpenAI 호출
   */
  @Transactional
  public float[] getOrCreateEmbedding(String userInput) {

    // 검색어 정규화
    String normalizedQuery = normalizeQuery(userInput);

    // DB조회
    return searchQueryCacheRepository.findByQueryText(normalizedQuery)
        .map(this::returnEmbedding)
        .orElseGet(() -> {
          float[] newEmbedding = openAIEmbeddingService.generateEmbedding(normalizedQuery);
          saveEmbeddingAsync(normalizedQuery, newEmbedding);
          return newEmbedding;
        });
  }

  /**
   * 캐시에 존재하는 Embedding 업데이트 및 반환
   */
  private float[] returnEmbedding(SearchQueryCache cache) {
    cache.setSearchCount(cache.getSearchCount() + 1);
    searchQueryCacheRepository.save(cache);
    log.info("캐싱된 Embedding 사용 - Query: {}, SearchCount: {}", cache.getQueryText(), cache.getSearchCount());
    return cache.getEmbedding();
  }

  /**
   * 새로운 Embedding 비동기 저장
   */
  @Async
  public void saveEmbeddingAsync(String queryText, float[] embedding) {
    SearchQueryCache newCache = SearchQueryCache.builder()
        .queryText(queryText)
        .embedding(embedding)
        .searchCount(1)
        .build();
    searchQueryCacheRepository.save(newCache);
    log.info("새 Embedding 생성 및 캐싱 - Query: {}", queryText);
  }


  /**
   * 검색어 정규화 trim + lowerCase + 다중 공백 제거
   */
  private String normalizeQuery(String input) {
    if (input == null) {
      return "";
    }

    String result = input.trim();
    result = result.toLowerCase();
    result = result.replaceAll("\\s", " ");

    return result;
  }
}
