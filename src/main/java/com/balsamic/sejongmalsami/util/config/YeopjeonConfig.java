package com.balsamic.sejongmalsami.util.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class YeopjeonConfig {

  // 엽전 보상 및 패널티
  @Value("${yeopjeon.receive-like-reward}")
  private int likeReward;

  @Value("${yeopjeon.receive-dislike-penalty}")
  private int dislikePenalty;

  @Value("${yeopjeon.send-like}")
  private int sendLike;

  @Value("${yeopjeon.attendance-bonus}")
  private int attendanceBonus;

  @Value("${yeopjeon.report-penalty}")
  private int reportPenalty;

  @Value("${yeopjeon.chaetaek-chosen}")
  private int chaetaekChosen;

  @Value("${yeopjeon.chaetaek-accept}")
  private int chaetaekAccept;

  @Value("${yeopjeon.create-question-post}")
  private int createQuestionPost;

  @Value("${yeopjeon.purchase-document}")
  private int purchaseDocument;

  @Value("${yeopjeon.copyright-violation}")
  private int copyrightViolation;

  @Value("${yeopjeon.report-reward}")
  private int reportReward;

  @Value("${yeopjeon.create-account}")
  private int createAccount;

  // 게시글 등급 접근 조건 (필요 엽전)
  @Value("${yeopjeon.jungin-requirement}")
  private int junginRequirement;

  @Value("${yeopjeon.yangban-requirement}")
  private int yangbanRequirement;

  @Value("${yeopjeon.king-requirement}")
  private int kingRequirement;

  // 게시글 상세보기 시 엽전 소모 비용
  @Value("${yeopjeon.view-jungin-cost}")
  private int viewJunginCost;

  @Value("${yeopjeon.view-yangban-cost}")
  private int viewYangbanCost;

  @Value("${yeopjeon.view-king-cost}")
  private int viewKingCost;
}
