package com.balsamic.sejongmalsami.post.object.mongo;

import com.balsamic.sejongmalsami.constants.LikeType;
import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;

import com.balsamic.sejongmalsami.constants.ContentType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "member_comment_idx", def = "{'memberId': 1, 'commentId': 1}")
public class CommentLike extends BaseMongoEntity {
  @Id
  private String commentLikeId;

  @Indexed
  @NotNull
  private UUID commentId;

  @Indexed
  @NotNull
  private UUID memberId;

  @NotNull
  private ContentType contentType;

  @NotNull
  private LikeType likeType;
}
