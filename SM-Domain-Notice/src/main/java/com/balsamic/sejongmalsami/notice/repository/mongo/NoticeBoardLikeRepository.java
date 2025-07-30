package com.balsamic.sejongmalsami.notice.repository.mongo;

import com.balsamic.sejongmalsami.notice.object.mongo.NoticeBoardLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeBoardLikeRepository extends MongoRepository<NoticeBoardLike, String> {
}
