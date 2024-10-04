package com.balsamic.sejongmalsami.object;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class NoticePostCommand {

  private UUID noticePostID;
  private String title;
  private String content;
  private Integer views;
  private Integer likes;
}
