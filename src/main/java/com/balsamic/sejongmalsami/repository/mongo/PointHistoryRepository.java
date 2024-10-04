package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.YeopjeonHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointHistoryRepository extends MongoRepository<YeopjeonHistory, String> {
}
