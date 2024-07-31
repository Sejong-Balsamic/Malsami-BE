package com.balsamic.sejongmalsami.member.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
  @Id
  private Long memberId;

  @Column(unique = true)
  private Long studentId;

  private String studentName;

  @Column(unique = true)
  private String uuidNickname;

  private String major;

  private String academicYear;

  private String enrollmentStatus;
}
