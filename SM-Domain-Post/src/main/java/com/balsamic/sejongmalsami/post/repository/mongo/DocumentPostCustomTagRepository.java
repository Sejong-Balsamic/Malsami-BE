package com.balsamic.sejongmalsami.post.repository.mongo;

import com.balsamic.sejongmalsami.post.object.mongo.DocumentPostCustomTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentPostCustomTagRepository extends MongoRepository<DocumentPostCustomTag, String> {

  // 단일 자료 게시글 태그 조회
  List<DocumentPostCustomTag> findAllByDocumentPostId(UUID documentPostId);
}