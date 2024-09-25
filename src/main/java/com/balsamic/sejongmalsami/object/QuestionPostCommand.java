package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QuestionPostCommand {
    private Long questionPostId;
    private String title;
    private String content;
    private String subject;
    private String writer;
    private Integer views;
    private Integer likes;
    private Integer answerCount;
    private Integer bounty;
    private Boolean isPrivate;
}
