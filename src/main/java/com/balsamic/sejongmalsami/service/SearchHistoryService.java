package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.CommonUtil.nullIfBlank;

import com.balsamic.sejongmalsami.object.QueryCommand;
import com.balsamic.sejongmalsami.object.QueryDto;
import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import com.balsamic.sejongmalsami.repository.mongo.SearchHistoryRepository;
import com.balsamic.sejongmalsami.util.RedisLockManager;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {

  private final SearchHistoryRepository searchHistoryRepository;
  private final RedisLockManager redisLockManager;

  private static final Long WAIT_TIME = 5L;
  private static final Long LEASE_TIME = 2L;

  /**
   * 사용자가 검색 로직 실행 시
   * 사용자가 검색어를 입력할 때마다 MongoDB에서 검색 횟수 +1 증가
   */
  @Async
  @Transactional
  public void increaseSearchCount(String keyword) {

    // 검색어 검증
    if (nullIfBlank(keyword) == null) {
      log.warn("검색어가 null이므로 점수 업데이트를 진행하지 않습니다.");
      throw new CustomException(ErrorCode.QUERY_EMPTY);
    }

    String lockKey = "lock:searchHistory:" + keyword;
    redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {
      log.debug("검색어 '{}'의 검색 횟수를 증가시킵니다.", keyword);

      // MongoDB에서 해당 검색어 조회
      SearchHistory searchHistory = searchHistoryRepository.findByKeyword(keyword);

      if (searchHistory != null) {
        // 기존 검색어인 경우 검색 횟수 증가
        searchHistory.increaseSearchCount();
        searchHistoryRepository.save(searchHistory);
        log.debug("기존 검색어 '{}'의 검색 횟수 증가 완료. 현재 횟수: {}", keyword, searchHistory.getSearchCount());
      } else {
        // 새로운 검색어인 경우 새로 생성
        SearchHistory newSearchHistory = SearchHistory.builder()
            .keyword(keyword)
            .searchCount(1L)
            .currentRank(-1)
            .lastRank(-1)
            .rankChange(0)
            .isNew(false)
            .build();
        searchHistoryRepository.save(newSearchHistory);
        log.debug("새로운 검색어 '{}' 생성 완료. 검색 횟수: 1", keyword);
      }
      return true;
    });
  }

  /**
   * 실시간 인기 검색어 TOP N 요청 시
   * MongoDB에서 검색 횟수가 높은 순으로 상위 N개 검색어 조회
   * 순위 변동 및 등락폭 계산
   */
  @Transactional(readOnly = true)
  public QueryDto getRealTimeTopKeywords(QueryCommand command) {

    log.debug("실시간 인기 검색어 TOP{}을 가져옵니다.", command.getTopN());

    String lockKey = "lock:topKeywords";
    return redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {
      // 검색 횟수가 높은 순으로 상위 N개 검색어 조회
      Pageable pageable = PageRequest.of(0, command.getTopN(), Sort.by("searchCount").descending());
      List<SearchHistory> topSearchHistories = searchHistoryRepository.findAll(pageable).getContent();

      if (topSearchHistories.isEmpty()) {
        log.debug("상위 검색어가 존재하지 않습니다.");
        return QueryDto.builder()
            .searchHistoryList(Collections.emptyList())
            .build();
      }

      // 순위 및 등락폭 계산
      List<SearchHistory> resultList = new ArrayList<>();
      int currentRank = 1;

      for (SearchHistory searchHistory : topSearchHistories) {
        // 이전 순위 저장
        Integer previousRank = searchHistory.getCurrentRank();
        
        // 현재 순위 설정
        searchHistory.setLastRank(previousRank);
        searchHistory.setCurrentRank(currentRank);

        // 등락폭 계산
        if (previousRank == null || previousRank == -1) {
          // 순위권에 새로 진입한 경우
          searchHistory.setIsNew(true);
          searchHistory.setRankChange(0);
          log.debug("검색어 '{}'이(가) 순위권에 새로 진입했습니다. 현재 순위: {}", searchHistory.getKeyword(), currentRank);
        } else {
          // 기존 순위권에 있던 경우
          int rankChange = previousRank - currentRank;
          searchHistory.setRankChange(rankChange);
          searchHistory.setIsNew(false);
          log.debug("검색어 '{}'의 순위 변동: {}위 → {}위 (변동: {})", 
              searchHistory.getKeyword(), previousRank, currentRank, rankChange);
        }

        resultList.add(searchHistory);
        currentRank++;
      }

      // 순위 정보를 MongoDB에 저장
      searchHistoryRepository.saveAll(resultList);

      return QueryDto.builder()
          .searchHistoryList(resultList)
          .build();
    });
  }
}
