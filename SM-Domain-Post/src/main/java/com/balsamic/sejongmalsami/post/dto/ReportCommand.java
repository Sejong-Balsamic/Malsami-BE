package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.ReportReason;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ReportCommand {
  public ReportCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }
  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;

  private UUID reportedEntityId;

  private ContentType contentType;

  private ReportReason reportReason;

  @Schema(defaultValue = "0")
  private Integer pageNumber;

  @Schema(defaultValue = "30")
  private Integer pageSize;
}
