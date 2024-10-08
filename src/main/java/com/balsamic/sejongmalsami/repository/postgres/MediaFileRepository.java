package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.MediaFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

  // 특정 질문글에 속한 파일의 개수 카운트
  @Query("SELECT COUNT(m) FROM MediaFile m WHERE m.postId = :postId AND m.postType = 'QUESTION'")
  int countByQuestionPost(UUID postId);

  // 특정 답변글에 속한 파일의 개수 카운트
  @Query("SELECT COUNT(m) FROM MediaFile m WHERE m.postId = :postId AND m.postType = 'ANSWER'")
  int countByAnswerPost(UUID postId);
}
