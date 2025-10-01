package com.balsamic.sejongmalsami.notice.repository.mongo;

import com.balsamic.sejongmalsami.notice.object.mongo.NoticeBoardLike;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeBoardLikeRepository extends MongoRepository<NoticeBoardLike, String> {

  Boolean existsByNoticePostIdAndMemberId(UUID noticePostId, UUID memberId);

  void deleteByNoticePostIdAndMemberId(UUID noticePostId, UUID memberId);
}
