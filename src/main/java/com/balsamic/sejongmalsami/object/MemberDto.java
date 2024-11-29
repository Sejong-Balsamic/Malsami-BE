package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ExpTier;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class MemberDto {

  private Member member;

  // auth
  private String major;
  private String studentIdString;
  private String studentName;
  private String academicYear;
  private String enrollmentStatus;

  // token
  private String accessToken;
  private String refreshToken;

  private Yeopjeon yeopjeon;
  private Exp exp;

  private Boolean isFirstLogin;
  private Boolean isAdmin;

  // Yeopjeon
  private Integer yeopjeonRank;
  private Integer totalYeopjeon;
  private Double yeopjeonPercentile;

  // Exp
  private Integer expRank;
  private Integer totalExp;
  private Double expPercentile;
  private ExpTier expTier;
  private Integer levelStartExp;
  private Integer levelEndExp;
  private Double progressPercent;

  // Total
  private Long totalLikeCount;
  private Long totalPopularPostCount;
  private Long totalCommentCount;
  private Long totalPostCount;
  private Long questionPostCount;
  private Long answerPostCount;
  private Long documentPostCount;
  private Long documentRequestPostCount;
}
