package com.balsamic.sejongmalsami.object;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class SearchHistoryCommand {

  public SearchHistoryCommand() {
    this.topN = 10;
  }

  private Integer topN;
}
