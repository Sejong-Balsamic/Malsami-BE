package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.SearchHistoryDto;
import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import com.balsamic.sejongmalsami.repository.mongo.SearchHistoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchHistoryService {

  private final SearchHistoryRepository searchHistoryRepository;

  private static final long SEARCH_RANKING_SCHEDULED_RATE = 30 * 60 * 1000L; // 30분

  /**
   * 시간마다 검색어 순위 업데이트
   */
  @Async
  @Transactional
  @Scheduled(fixedRate = SEARCH_RANKING_SCHEDULED_RATE)
  public void updateSearchRanking() {
    log.debug("인기 검색어 순위 업데이트를 시작합니다");
    List<SearchHistory> topKeywords = searchHistoryRepository
        .findTop10ByOrderBySearchCountDesc();

    int rank = 1;
    for (SearchHistory keyword : topKeywords) {
      keyword.setIsNew(false);
      int previousRank = keyword.getCurrentRank(); // 이전 순위

      keyword.setLastRank(previousRank); // 이전 순위 등록
      keyword.setCurrentRank(rank); // 현재 순위 등록 (1위부터 순차적 진행)

      // 이전 순위가 없는 경우 New 표시
      if (previousRank == -1) {
        keyword.setIsNew(true);
      } else { // 등락폭 계산
        keyword.setRankChange(previousRank - rank);
      }

      searchHistoryRepository.save(keyword);
      rank++;
    }
    log.debug("인기 검색어 순위 업데이트가 완료되었습니다");
  }

  /**
   * 상위 10개 인기 검색어 조회 로직
   */
  @Transactional(readOnly = true)
  public SearchHistoryDto getTopKeywords() {
    return SearchHistoryDto.builder()
        .searchHistoryList(searchHistoryRepository.findTop10ByOrderBySearchCountDesc())
        .build();
  }
}
