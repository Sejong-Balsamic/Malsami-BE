package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminCommand {
  private Member member;
  private Integer amount;
  private UUID targetMemberId;

  private Long studentId;
  private String studentName;
  private UUID memberId;
  private String memberIdStr;
  private String uuidNickname;

  private Integer pageNumber;
  private Integer pageSize;
  private String sortField;
  private String sortDirection;
}
