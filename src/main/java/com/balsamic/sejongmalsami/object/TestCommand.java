package com.balsamic.sejongmalsami.object;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class TestCommand {
  @Schema(defaultValue = "/projects/sejong-malsami/document/DOCUMENT_sejong-malsami-test_b482d4cc-722c-42e7-b042-b4a3d5903ae5.mp4")
  private String filePath;
}
