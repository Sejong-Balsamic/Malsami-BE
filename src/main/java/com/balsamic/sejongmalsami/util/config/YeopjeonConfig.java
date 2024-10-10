package com.balsamic.sejongmalsami.util.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class YeopjeonConfig {

  // 엽전 보상 및 패널티
  public static final int LIKE_REWARD = 5;
  public static final int DISLIKE_PENALTY = 3;
  public static final int ATTENDANCE_BONUS = 200;
  public static final int REPORT_PENALTY = 100;
  public static final int CHAETAEK = 100;

  // 게시글 등급 접근 조건 (필요 엽전)
  public static final int JUNGIN_REQUIREMENT = 1000;
  public static final int YANGBAN_REQUIREMENT = 10000;
  public static final int KING_REQUIREMENT = 50000;

  // 게시글 상세보기 시 엽전 소모 비용
  public static final int VIEW_JUNGIN_COST = 10;
  public static final int VIEW_YANGBAN_COST = 20;
  public static final int VIEW_KING_COST = 50;
}
