package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.DocumentPostCustomTag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentPostCustomTagRepository extends MongoRepository<DocumentPostCustomTag, String> {
}
