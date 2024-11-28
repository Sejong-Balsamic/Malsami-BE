package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

  // 특정 글에 속한 파일의 개수 카운트
  @Query("SELECT COUNT(m) FROM MediaFile m WHERE m.postId = :postId AND m.contentType = :contentType")
  int countByPost(UUID postId, ContentType contentType);

  List<MediaFile> findAllByPostId(UUID postId);

  List<MediaFile> findAllByPostIdAndContentType(UUID postId, ContentType contentType);
}
