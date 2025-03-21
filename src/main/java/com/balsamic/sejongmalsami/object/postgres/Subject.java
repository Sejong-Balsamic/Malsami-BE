package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
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
public class Subject extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

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

  // 일간 질문게시글 개수
  private Long dailyQuestionScore;

  // 주간 질문게시글 개수
  private Long weeklyQuestionScore;

  // 달간 질문게시글 개수
  private Long monthlyQuestionScore;

  // 전체 질문게시글 개수
  private Long totalQuestionScore;
}