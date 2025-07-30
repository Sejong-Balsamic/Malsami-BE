package com.balsamic.sejongmalsami.post.repository.mongo;

import com.balsamic.sejongmalsami.post.object.mongo.CommentLike;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentLikeRepository extends MongoRepository<CommentLike, String> {

  Boolean existsByCommentIdAndMemberId(UUID commentId, UUID memberId);

  List<CommentLike> findAllByCommentIdInAndMemberId(List<UUID> commentIds, UUID memberId);

  long countByCommentIdIn(List<UUID> commentIds);
}
