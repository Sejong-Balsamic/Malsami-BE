package com.balsamic.sejongmalsami.member.dto;

import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.TestMember;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class MemberDto {

  private Member member;
  private TestMember testMember;
  private Page<TestMember> testMembersPage;

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
  private Integer totalYeopjeonMembers;
  private Double yeopjeonPercentile;

  private Boolean canAccessCheonmin;
  private Boolean canAccessJungin;
  private Boolean canAccessYangban;
  private Boolean canAccessKing;

  private Integer cheonminRequirement;
  private Integer junginRequirement;
  private Integer yangbanRequirement;
  private Integer kingRequirement;

  // Exp
  private Integer expRank;
  private Integer totalExpMembers;
  private Double expPercentile;

  // Total
  private Long totalLikeCount;
  private Long totalPopularPostCount;
  private Long totalCommentCount;
  private Long totalPostCount;
  private Long questionPostCount;
  private Long answerPostCount;
  private Long documentPostCount;
  private Long documentRequestPostCount;

  private Page<Member> membersPage;
}
