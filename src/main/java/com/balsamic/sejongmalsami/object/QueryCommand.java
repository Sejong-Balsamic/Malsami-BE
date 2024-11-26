package com.balsamic.sejongmalsami.object;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class QueryCommand {

  public QueryCommand() {
    pageNumber = 0;
    pageSize = 30;
  }

  private String query;
  private String subject;
  @Schema(defaultValue = "0")
  private Integer pageNumber;
  @Schema(defaultValue = "30")
  private Integer pageSize;
}
