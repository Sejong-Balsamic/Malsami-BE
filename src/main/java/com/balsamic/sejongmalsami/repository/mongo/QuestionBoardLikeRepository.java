package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.QuestionBoardLike;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionBoardLikeRepository extends MongoRepository<QuestionBoardLike, String> {

  Boolean existsByQuestionBoardIdAndMemberId(UUID postId, UUID memberId);
}
