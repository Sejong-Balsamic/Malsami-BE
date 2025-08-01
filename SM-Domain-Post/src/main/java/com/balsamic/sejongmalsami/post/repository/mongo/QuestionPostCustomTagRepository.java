package com.balsamic.sejongmalsami.post.repository.mongo;

import com.balsamic.sejongmalsami.post.object.mongo.QuestionPostCustomTag;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionPostCustomTagRepository extends MongoRepository<QuestionPostCustomTag, String> {

  Boolean existsByQuestionPostId(UUID questionPostId);

  List<QuestionPostCustomTag> findAllByQuestionPostId(UUID questionPostId);

  List<QuestionPostCustomTag> findAllByQuestionPostIdIn(Set<UUID> questionPostIds);
}