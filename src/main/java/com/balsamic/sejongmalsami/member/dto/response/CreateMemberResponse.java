package com.balsamic.sejongmalsami.member.dto.response;

import com.balsamic.sejongmalsami.member.domain.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateMemberResponse {
  private Long memberId;
  private Long studentId;
  private String studentName;
  private String uuidNickName;

  public static CreateMemberResponse from(Member member) {
    return CreateMemberResponse.builder()
        .memberId(member.getMemberId())
        .studentId(member.getStudentId())
        .studentName(member.getStudentName())
        .uuidNickName(member.getUuidNickname())
        .build();
  }
}
