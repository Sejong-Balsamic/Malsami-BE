package com.balsamic.sejongmalsami.mongo;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.LikeType;
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
@CompoundIndex(name = "member_document_idx", def = "{'memberId': 1, 'documentId': 1}")
public class DocumentBoardLike extends BaseMongoEntity {

  @Id
  private String documentBoardLikeId;

  @Indexed
  @NotNull
  private UUID memberId;

  @Indexed
  @NotNull
  private UUID documentBoardId; // 자료 글 or 자료 요청 글 PK

  @NotNull
  private ContentType contentType;

  @NotNull
  private LikeType likeType;
}
