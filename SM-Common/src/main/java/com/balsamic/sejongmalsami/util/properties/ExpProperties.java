package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class ExpProperties {

  // 질문 글 작성
  @Value("${exp.create-question-post}")
  private int createQuestionPost;

  // 답변 글 작성
  @Value("${exp.create-answer-post}")
  private int createAnswerPost;

  // 자료 글 작성
  @Value("${exp.create-document-post}")
  private int createDocumentPost;

  // 댓글 작성
  @Value("${exp.create-comment}")
  private int createComment;

  // 자료 구매
  @Value("${exp.purchase-document}")
  private int purchaseDocument;

  // 채택 받음
  @Value("${exp.chaetaek-chosen}")
  private int chaetaekChosen;

  // 채택 누름
  @Value("${exp.chaetaek-accept}")
  private int chaetaekAccept;

  // 좋아요 받음
  @Value("${exp.receive-like}")
  private int receiveLike;
}
