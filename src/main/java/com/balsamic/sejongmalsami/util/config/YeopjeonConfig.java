package com.balsamic.sejongmalsami.util.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class YeopjeonConfig {

  // 엽전 보상 및 패널티
  @Value("${yeopjeon.like-reward}")
  private int likeReward;

  @Value("${yeopjeon.dislike-penalty}")
  private int dislikePenalty;

  @Value("${yeopjeon.attendance-bonus}")
  private int attendanceBonus;

  @Value("${yeopjeon.report-penalty}")
  private int reportPenalty;

  @Value("${yeopjeon.chaetaek}")
  private int chaetaek;

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
