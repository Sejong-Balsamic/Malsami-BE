package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class YeopjeonProperties {

  // 엽전 보상 및 패널티
  @Value("${yeopjeon.receive-like-reward}")
  private Integer likeReward;

  @Value("${yeopjeon.receive-dislike-penalty}")
  private Integer dislikePenalty;

  @Value("${yeopjeon.attendance-bonus}")
  private Integer attendanceBonus;

  @Value("${yeopjeon.report-penalty}")
  private Integer reportPenalty;

  @Value("${yeopjeon.chaetaek-chosen}")
  private Integer chaetaekChosen;

  @Value("${yeopjeon.chaetaek-accept}")
  private Integer chaetaekAccept;

  @Value("${yeopjeon.create-question-post}")
  private Integer createQuestionPost;

  @Value("${yeopjeon.purchase-document}")
  private Integer purchaseDocument;

  @Value("${yeopjeon.file-uploader-reward}")
  private Integer DocumentFileUploaderReward;

  @Value("${yeopjeon.copyright-violation}")
  private Integer copyrightViolation;

  @Value("${yeopjeon.report-reward}")
  private Integer reportReward;

  @Value("${yeopjeon.create-account}")
  private Integer createAccount;

  // 게시글 등급 접근 조건 (필요 엽전)
  @Value("${yeopjeon.cheonmin-requirement}")
  private Integer cheonminRequirement;

  @Value("${yeopjeon.jungin-requirement}")
  private Integer junginRequirement;

  @Value("${yeopjeon.yangban-requirement}")
  private Integer yangbanRequirement;

  @Value("${yeopjeon.king-requirement}")
  private Integer kingRequirement;

  // 게시글 상세보기 시 엽전 소모 비용
  @Value("${yeopjeon.view-cheonmin-cost}")
  private Integer viewCheonminCost;

  @Value("${yeopjeon.view-jungin-cost}")
  private Integer viewJunginCost;

  @Value("${yeopjeon.view-yangban-cost}")
  private Integer viewYangbanCost;

  @Value("${yeopjeon.view-king-cost}")
  private Integer viewKingCost;
}
