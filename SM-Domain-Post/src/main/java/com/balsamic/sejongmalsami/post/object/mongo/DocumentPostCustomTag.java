package com.balsamic.sejongmalsami.post.object.mongo;

import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPostCustomTag extends BaseMongoEntity {

  @Id
  private String documentPostCustomTagId;

  @Indexed
  @NotNull
  private UUID documentPostId;

  private String customTag;
}