package com.balsamic.sejongmalsami.object;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberYeopjeon {
  private UUID memberId;
  private Long studentId;
  private String studentName;
  private String uuidNickname;
  private String major;
  private Integer yeopjeon;
}
