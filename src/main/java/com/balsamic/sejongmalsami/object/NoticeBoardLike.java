package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.LikeType;
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
  private LikeType likeType; // POST, COMMENT

  @NotNull
  @Indexed
  private UUID noticeBoardId; // target UUID

  @NotNull
  @Indexed
  private UUID memberId;
}
