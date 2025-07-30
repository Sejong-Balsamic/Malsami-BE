package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YeopjeonHistoryRepository extends MongoRepository<YeopjeonHistory, String> {
}
