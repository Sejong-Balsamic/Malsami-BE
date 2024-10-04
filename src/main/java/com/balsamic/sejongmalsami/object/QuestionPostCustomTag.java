package com.balsamic.sejongmalsami.object;

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
public class QuestionPostCustomTag {

  @Id
  private String questionPostCustomTagId;

  @Indexed
  private String questionPostId;

  private String customTag;
}
