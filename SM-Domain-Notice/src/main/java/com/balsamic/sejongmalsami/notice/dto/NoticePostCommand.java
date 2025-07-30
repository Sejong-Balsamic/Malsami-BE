package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.constants.SortType;
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
public class NoticePostCommand {

  public NoticePostCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

  private Member member;
  private String title;
  private String content;
  private UUID noticePostId;

  // 필터링
  private String query;
  private SortType sortType;
  private String sortField;
  private String sortDirection;

  @Schema(defaultValue = "0")
  private Integer pageNumber;
  @Schema(defaultValue = "30")
  private Integer pageSize;
}
