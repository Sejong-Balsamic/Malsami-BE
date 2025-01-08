package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.ReportReason;
import com.balsamic.sejongmalsami.object.postgres.Member;
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

  private UUID memberId;

  private Member member;

  private UUID reportedId;

  private UUID reportedEntityId;

  private ContentType contentType;

  private ReportReason reportReason;

  private String message;

  @Schema(defaultValue = "0")
  private Integer pageNumber = 0;

  @Schema(defaultValue = "30")
  private Integer pageSize = 30;
}
