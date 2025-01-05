package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestCommand {
  public TestCommand() {
    postCount = 30;
  }

  private Member member;

  @Schema(defaultValue = "30")
  private Integer postCount;

  @Schema(defaultValue = "/projects/sejong-malsami/document/DOCUMENT_sejong-malsami-test_b482d4cc-722c-42e7-b042-b4a3d5903ae5.mp4")
  private String filePath;

}
