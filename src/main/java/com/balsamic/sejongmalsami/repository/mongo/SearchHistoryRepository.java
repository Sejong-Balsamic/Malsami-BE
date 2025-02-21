package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SearchHistoryRepository extends MongoRepository<SearchHistory, String> {

  Optional<SearchHistory> findByKeyword(String keyword);

  // 검색 횟수가 높은 순서대로 상위 10개 검색어 조회
  List<SearchHistory> findTop10ByOrderBySearchCountDesc();
}
