package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 교과목명
 */
@Entity
@Getter
@NoArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
public class Subject extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID subjectId;

  @Column(unique = true, nullable = false)
  private String name; // 교과목명

  // 일간 자료게시글 개수
  private Long dailyDocumentScore;

  // 주간 자료게시글 개수
  private Long weeklyDocumentScore;

  // 달간 자료게시글 개수
  private Long monthlyDocumentScore;

  // 전체 자료게시글 개수
  private Long totalDocumentScore;
}