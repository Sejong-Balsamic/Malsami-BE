package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
public class Course extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID courseId;

  private String subject; // 교과목명

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "faculty_id", nullable = false)
  private Faculty faculty; // 단과대학

  private String department; // 학과
  private Integer year; // 년도
  private Integer semester; // 학기
}
