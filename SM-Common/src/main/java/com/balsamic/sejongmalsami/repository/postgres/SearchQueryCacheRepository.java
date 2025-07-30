package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.postgres.SearchQueryCache;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchQueryCacheRepository extends JpaRepository<SearchQueryCache, UUID> {

  // 검색어로 조회
  Optional<SearchQueryCache> findByQueryText(String queryText);
}
