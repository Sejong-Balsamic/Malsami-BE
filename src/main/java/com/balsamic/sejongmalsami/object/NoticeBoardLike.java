package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.LikeType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeBoardLike extends BaseMongoEntity {
  @Id
  private String noticeBoardLikeId;

  private UUID noticeId;

  private UUID memberId;

  private LikeType likeType; // POST, COMMENT
}
