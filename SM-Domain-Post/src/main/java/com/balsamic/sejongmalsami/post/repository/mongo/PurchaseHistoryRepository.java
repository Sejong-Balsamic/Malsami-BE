package com.balsamic.sejongmalsami.post.repository.mongo;

import com.balsamic.sejongmalsami.post.object.mongo.PurchaseHistory;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseHistoryRepository extends MongoRepository<PurchaseHistory, String> {

  boolean existsByMemberIdAndDocumentFileId(UUID memberId, UUID documentFileId);
}
