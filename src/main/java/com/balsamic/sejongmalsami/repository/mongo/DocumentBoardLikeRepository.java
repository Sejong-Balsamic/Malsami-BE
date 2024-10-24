package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.DocumentBoardLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentBoardLikeRepository extends MongoRepository<DocumentBoardLike, String> {
}