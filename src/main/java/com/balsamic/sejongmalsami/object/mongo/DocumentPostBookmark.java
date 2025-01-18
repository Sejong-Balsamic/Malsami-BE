package com.balsamic.sejongmalsami.object.mongo;

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
@CompoundIndex(name = "member_post_bookmark_idx", def = "{'memberId': 1, 'documentPostId': 1}", unique = true)
public class DocumentPostBookmark extends BaseMongoEntity {

  @Id
  private String documentPostBookmarkId;

  @Indexed
  @NotNull
  private UUID documentPostId;

  @Indexed
  @NotNull
  private UUID memberId;
}
