package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.CommonUtil.nullIfBlank;

import com.balsamic.sejongmalsami.object.SearchHistoryCommand;
import com.balsamic.sejongmalsami.object.SearchHistoryDto;
import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import com.balsamic.sejongmalsami.repository.mongo.SearchHistoryRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {

  private final SearchHistoryRepository searchHistoryRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  private static final String SEARCH_RANK_KEY = "searchRank";

  /**
   * 사용자가 검색 로직 실행 시
   * 사용자가 검색어를 입력할 때마다 Redis 점수 +1 증가
   * Redis Sorted Set을 통한 실시간 업데이트
   */
  @Async
  @Transactional
  public void increaseSearchCount(String keyword) {

    // 검색어 검증
    if (nullIfBlank(keyword) == null) {
      log.warn("검색어가 null이므로 점수 업데이트를 진행하지 않습니다.");
      throw new CustomException(ErrorCode.QUERY_EMPTY);
    }

    Double currentScore = redisTemplate.opsForZSet().score(SEARCH_RANK_KEY, keyword);

    // Redis에 없는 검색어 -> MongoDB에서 searchCount 확인
    if (currentScore == null) {
      log.debug("Redis에 없는 새로운 검색어가 입력되어 MongoDB를 확인합니다.");
      SearchHistory searchHistory = searchHistoryRepository.findByKeyword(keyword);
      if (searchHistory == null) { // MongoDB에도 저장되어있지 않은 새로운 검색어인경우
        log.debug("MongoDB에도 저장되지 않은 새로운 검색어입니다.");
        currentScore = 0.0;
      } else { // MongoDB에 저장되어있는 검색어인 경우
        log.debug("MongoDB에 저장되어있는 데이터를 Redis에 할당합니다.");
        currentScore = (double) searchHistory.getSearchCount();
      }

      // Redis에 저장
      redisTemplate.opsForZSet().add(SEARCH_RANK_KEY, keyword, currentScore);
      log.debug("Redis에 {}점으로 초기화 (MongoDB 기준)", currentScore);
    }

    // 해당 검색어 점수 업데이트
    redisTemplate.opsForZSet().incrementScore(SEARCH_RANK_KEY, keyword, 1.0);
    log.debug("검색어: {} -> Redis searchCount 업데이트 완료", keyword);
  }

  /**
   * 실시간 인기 검색어 TOP10 요청 시
   * 1. Redis에 저장된 상위 N개 검색어 추출
   * 2. 각 검색어에 대해 MongoDB에 저장된 값 업데이트
   */
  public SearchHistoryDto getRealTimeTopKeywords(SearchHistoryCommand command) {

    log.debug("실시간 인기 검색어 TOP{}을 가져옵니다.", command.getTopN());
    // 1. Redis에서 점수가 높은 순으로 상위 N개 검색어 가져오기
    Set<ZSetOperations.TypedTuple<Object>> topSet = redisTemplate.opsForZSet()
        .reverseRangeWithScores(
            SEARCH_RANK_KEY, 0, command.getTopN() - 1);

    // 상위 검색어 set이 비어있는 경우 빈 리스트 반환
    if (topSet == null || topSet.isEmpty()) {
      log.debug("상위 검색어가 존재하지 않습니다.");
      return SearchHistoryDto.builder()
          .searchHistoryList(Collections.emptyList())
          .build();
    }

    // 2. 순위가 높은 순으로 등락 폭/신규 진입 여부 계산
    List<SearchHistory> resultList = new ArrayList<>();
    int rank = 1;

    double scoreDouble;
    for (ZSetOperations.TypedTuple<Object> tuple : topSet) {
      if (tuple.getValue() == null) { // Redis에 저장된 검색어가 null 인 경우
        log.warn("Redis에 저장된 검색어가 null이므로 건너뜁니다");
        continue;
      } else if (nullIfBlank(tuple.getScore()) == null) { // 검색어 점수가 0.0 인 경우
        scoreDouble = 0.0;
      } else { // 검색어와 점수가 모두 존재하는 경우
        scoreDouble = tuple.getScore();
      }

      String keyword = tuple.getValue().toString();
      long currentSearchCount = (long) scoreDouble;

      // 3. MongoDB에서 이전 검색어 이력 조회
      SearchHistory searchHistory = searchHistoryRepository.findByKeyword(keyword);

      // MongoDB에 없는 경우 -> 새로 생성
      if (searchHistory == null) {
        searchHistory = SearchHistory.builder()
            .keyword(keyword)
            .searchCount(currentSearchCount)
            .currentRank(rank)
            .lastRank(-1)
            .rankChange(0)
            .isNew(true)
            .build();
        log.debug("MongoDB에 없는 새로운 검색어입니다. 해당 검색어를 저장합니다. 검색어: {}", keyword);
      } else { // MongoDB에 기존 데이터가 있는 경우 -> 등록 폭 및 신규 진입 여부 계산
        log.debug("MongoDB에 저장되어있는 검색어입니다. 순위 및 변동폭을 업데이트합니다. 검색어: {}", keyword);
        searchHistory.setIsNew(false);
        searchHistory.setSearchCount(currentSearchCount); // MongoDB 검색 횟수 Redis값으로 업데이트

        int previousRank = searchHistory.getCurrentRank() == null ? -1 : searchHistory.getCurrentRank();
        searchHistory.setLastRank(previousRank);
        searchHistory.setCurrentRank(rank);

        if (previousRank == -1) { // 순위권에 새로 진입한 경우
          searchHistory.setIsNew(true); // isNew = true 설정
          searchHistory.setRankChange(0); // 새로 들어온 경우 등락폭 0 설정
        } else { // 기존에 순위권에 있던 키워드인경우
          int rankChange = previousRank - rank;
          searchHistory.setRankChange(rankChange);
          searchHistory.setIsNew(false);
        }
      }

      // 4. MongoDB 저장 (등락 폭 / 검색 횟수 / 순위)
      resultList.add(searchHistoryRepository.save(searchHistory));
      rank++;
    }
    return SearchHistoryDto.builder()
        .searchHistoryList(resultList)
        .build();
  }
}
