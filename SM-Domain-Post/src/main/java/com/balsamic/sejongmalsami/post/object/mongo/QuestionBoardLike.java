package com.balsamic.sejongmalsami.post.object.mongo;

import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;

import com.balsamic.sejongmalsami.constants.ContentType;
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

  @Indexed
  @NotNull
  private UUID memberId; // 좋아요를 누른 사용자 ID

  @Indexed
  @NotNull
  private UUID questionBoardId; // 질문글 or 답변 UUID

  @NotNull
  private ContentType contentType; // Question, Answer
}
