package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.Report;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ReportDto {
  private Report report;
}
