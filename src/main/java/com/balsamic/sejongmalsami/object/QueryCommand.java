package com.balsamic.sejongmalsami.object;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class QueryCommand {

  private String query;
  private String subject;
  @Schema(defaultValue = "0")
  private Integer pageNumber = 0;
  @Schema(defaultValue = "30")
  private Integer pageSize = 30;
}
