package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class MemberCommand {

  private UUID memberId;
  private Long studentId;
  private String studentIdString;
  private String studentName;
  private String uuidNickname;
  private String major;
  private String academicYear;
  private String enrollmentStatus;

  private Member member;

  // auth
  private String sejongPortalId;
  private String sejongPortalPassword;
}
