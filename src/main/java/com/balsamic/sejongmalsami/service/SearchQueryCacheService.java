package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.SearchQueryCache;
import com.balsamic.sejongmalsami.repository.postgres.SearchQueryCacheRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchQueryCacheService {

  private final SearchQueryCacheRepository searchQueryCacheRepository;
  private final OpenAIEmbeddingService openAIEmbeddingService;

  /**
   * 동일 검색어에 대한 Embedding 캐싱 처리
   * DB에 존재한다면 해당 검색어 Embedding값 반환
   * DB에 없는 경우 OpenAI 호출
   */
  @Transactional
  public float[] getOrCreateEmbedding(String userInput) {

    // 검색어 정규화
    String normalizedQuery = normalizeQuery(userInput);

    // DB조회
    Optional<SearchQueryCache> optionalCache = searchQueryCacheRepository.findByQueryText(normalizedQuery);

    if (optionalCache.isPresent()) {
      // 캐시에 존재하는 경우
      SearchQueryCache cache = optionalCache.get();
      cache.setSearchCount(cache.getSearchCount() + 1);
      searchQueryCacheRepository.save(cache);

      log.info("캐싱된 Embedding 사용 - Query: {}, SearchCount: {}", normalizedQuery, cache.getSearchCount());
      return cache.getEmbedding();
    } else {
      // 캐시에 없는 경우
      float[] newEmbedding = openAIEmbeddingService.generateEmbedding(normalizedQuery);

      SearchQueryCache cache = SearchQueryCache.builder()
          .queryText(normalizedQuery)
          .embedding(newEmbedding)
          .searchCount(1)
          .build();

      log.info("새 Embedding 생성 및 캐싱 - Query: {}", normalizedQuery);
      return newEmbedding;
    }
  }

  /**
   * 검색어 정규화
   * trim + lowerCase + 다중 공백 제거
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
