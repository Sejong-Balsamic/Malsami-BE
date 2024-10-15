package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.PurchaseHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseHistoryRepository extends MongoRepository<PurchaseHistory, String> {
}
