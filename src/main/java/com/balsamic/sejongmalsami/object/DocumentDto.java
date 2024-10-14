package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class DocumentDto {

  private DocumentPost documentPost; // 자료

  private DocumentRequestPost documentRequestPost; // 자료요청

}
