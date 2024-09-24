package com.balsamic.sejongmalsami.object;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
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
}
