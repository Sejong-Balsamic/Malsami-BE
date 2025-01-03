package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
public class EmbeddingCommand {
  private String text;
  private Float threshold;
  private ContentType contentType;
  private Integer pageSize;
  private Integer pageNumber;
}
