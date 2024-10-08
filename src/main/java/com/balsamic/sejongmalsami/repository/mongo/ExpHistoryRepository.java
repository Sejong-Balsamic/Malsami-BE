package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.ExpHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpHistoryRepository extends MongoRepository<ExpHistory, String> {
}
