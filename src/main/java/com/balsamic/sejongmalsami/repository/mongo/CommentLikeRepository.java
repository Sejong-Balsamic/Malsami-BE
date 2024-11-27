package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.CommentLike;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {

  Boolean existsByCommentIdAndMemberId(UUID commentId, UUID memberId);

  List<CommentLike> findAllByCommentIdInAndMemberId(List<UUID> commentIds, UUID memberId);

  long countByCommentIdIn(List<UUID> commentIds);
}
