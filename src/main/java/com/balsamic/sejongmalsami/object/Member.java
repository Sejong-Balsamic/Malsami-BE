package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "member_id", columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID memberId;

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

  private LocalDateTime lastLoginTime;
}
