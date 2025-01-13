package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.PurchaseHistory;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseHistoryRepository extends MongoRepository<PurchaseHistory, String> {

  boolean existsByMemberAndDocumentFile(Member member, DocumentFile documentFile);
}
