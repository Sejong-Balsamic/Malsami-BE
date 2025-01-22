package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
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

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "member_faculties", joinColumns = @JoinColumn(name = "member_id"))
  @Builder.Default
  private List<String> faculties = new ArrayList<>();

  @Builder.Default
  @Column(nullable = false)
  private Boolean isNotificationEnabled = true;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
  @Column(name = "role")
  @Builder.Default
  private Set<Role> roles = new HashSet<>();


  @Builder.Default
  @Enumerated(EnumType.STRING)
  private AccountStatus accountStatus = AccountStatus.ACTIVE;

  private LocalDateTime lastLoginTime;

  @Builder.Default
  @JsonIgnore
  private Boolean isFirstLogin = true;

  public void disableFirstLogin() {
    this.isFirstLogin = false;
  }
}
