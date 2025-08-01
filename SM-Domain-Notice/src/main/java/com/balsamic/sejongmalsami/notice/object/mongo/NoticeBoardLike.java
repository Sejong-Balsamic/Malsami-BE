package com.balsamic.sejongmalsami.notice.object.mongo;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeBoardLike extends BaseMongoEntity {
  @Id
  private String noticeBoardLikeId;

  @NotNull
  private ContentType contentType;

  @NotNull
  @Indexed
  private UUID noticePostId; // target UUID

  @NotNull
  @Indexed
  private UUID memberId;
}
