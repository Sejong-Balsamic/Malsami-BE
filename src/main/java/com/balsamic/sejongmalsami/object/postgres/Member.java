package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
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

  @Builder.Default
  @JsonIgnore
  private Boolean isFirstLogin = true;

  public void updateLastLoginTime(LocalDateTime lastLoginTime) {
    this.lastLoginTime = lastLoginTime;
  }

  public void disableFirstLogin() {
    this.isFirstLogin = false;
  }
}
