package com.balsamic.sejongmalsami.object.postgres;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class TestMember extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID testMemberId;

  @Column(unique = true, nullable = false)
  private Long testStudentId; // 9로 시작하는 학번

  @Column(nullable = false)
  private String password;

  private String testStudentName; // 테스트_홍길동

  private String testMajor;

  private String testAcademicYear;

  private String testEnrollmentStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member createdBy; // 생성한 관리자

  @Builder.Default
  private Boolean isActive = true;
}