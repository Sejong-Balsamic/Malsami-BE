package com.balsamic.sejongmalsami.util.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PostTierProperties {

  // 자료 게시글 등급
  @Value("${post-tier.like-requirement.cheonmin}")
  private Integer likeRequirementCheonmin;

  @Value("${post-tier.like-requirement.jungin}")
  private Integer likeRequirementJungin;

  @Value("${post-tier.like-requirement.yangban}")
  private Integer likeRequirementYangban;

  @Value("${post-tier.like-requirement.king}")
  private Integer likeRequirementKing;
}
