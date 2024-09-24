package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@ToString
public class QuestionPostCommand {
    private Long questionPostId;
    private String title;
    private String content;
    private String subject;
    private String writer;
    private int views;
    private int likes;
    private int answerCount;
    private int bounty;
    private Boolean isPrivate;
}
