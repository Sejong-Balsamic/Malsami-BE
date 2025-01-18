package com.balsamic.sejongmalsami.repository.mongo;


import com.balsamic.sejongmalsami.object.mongo.DocumentPostBookmark;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DocumentPostBookmarkRepository extends MongoRepository<DocumentPostBookmark, String> {

  Optional<DocumentPostBookmark> findByMemberIdAndDocumentPostId(UUID memberId, UUID documentPostId);

  void deleteByMemberIdAndDocumentPostId(UUID memberId, UUID documentPostId);
}