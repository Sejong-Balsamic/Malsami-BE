package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.QuestionPostCustomTag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPostCustomTagRepository extends MongoRepository<QuestionPostCustomTag, String> {
}