package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.mongo.DocumentBoardLike;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentBoardLikeRepository extends MongoRepository<DocumentBoardLike, String> {

  Boolean existsByDocumentBoardIdAndMemberId(UUID postId, UUID memberId);

  long countByDocumentBoardIdIn(List<UUID> documentBoardIds);
}