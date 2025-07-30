package com.balsamic.sejongmalsami.application.dto;

import com.balsamic.sejongmalsami.object.postgres.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class TestCommand {
  public TestCommand() {
    postCount = 30;
  }
  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;

  @Schema(defaultValue = "30")
  private Integer postCount;

  @Schema(defaultValue = "/projects/sejong-malsami/document/DOCUMENT_sejong-malsami-test_b482d4cc-722c-42e7-b042-b4a3d5903ae5.mp4")
  private String filePath;

  private boolean useMockMember; // true: 가짜 Member 사용, false: 실제 Member 사용
}
