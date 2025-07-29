package com.balsamic.sejongmalsami.ai.dto;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
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

  @Schema(defaultValue = "0.75")
  private Float threshold;

  private ContentType contentType;

  @Schema(defaultValue = "10")
  private Integer pageSize;

  @Schema(defaultValue = "0")
  private Integer pageNumber;
}
