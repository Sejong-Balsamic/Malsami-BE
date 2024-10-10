package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
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
public class QuestionBoardLike extends BaseMongoEntity {

  @Id
  private String questionBoardLikeId;

  @NotNull
  private ContentType contentType;

  @Indexed
  @NotNull
  private UUID questionBoardId; // target UUID

  @Indexed
  @NotNull
  private UUID memberId;      // 좋아요를 누른 사용자 ID
}
