package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class QuestionPostCommand {

  private String memberId;
  private String title;
  private String content;
  private String subject;
  private Integer views;
  private Integer likes;
  private Integer answerCount;
  private Integer reward;
  private Boolean isPrivate;
}
