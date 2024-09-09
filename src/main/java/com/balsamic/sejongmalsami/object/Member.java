package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Member extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberId;

  @Column(unique = true)
  private Long studentId;

  private String studentName;

  @Column(unique = true)
  private String uuidNickname;

  private String major;

  private String academicYear;

  private String enrollmentStatus;

  @Column
  private String profileUrl;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isNotificationEnabled = true;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Role role = Role.ROLE_USER;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private AccountStatus accountStatus = AccountStatus.ACTIVE;

  // 마지막 로그인 일시
  private LocalDateTime lastLoginTime = LocalDateTime.now();

}
