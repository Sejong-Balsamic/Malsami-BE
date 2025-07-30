package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.mongo.NoticeBoardLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeBoardLikeRepository extends MongoRepository<NoticeBoardLike, String> {
}
