package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class YeopjeonDto {

  private Yeopjeon yeopjeon;
  private YeopjeonHistory yeopjeonHistory;
}
