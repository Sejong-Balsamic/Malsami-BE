package com.balsamic.sejongmalsami.object;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class DocumentDto {

  private DocumentPost documentPost; // 자료

  private List<DocumentPost> documentPosts; // 자료

  private DocumentRequestPost documentRequestPost; // 자료요청

  private List<DocumentRequestPost> documentRequestPosts; // 자료요청

}
