package com.balsamic.sejongmalsami.object;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class AnswerPost extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID answerPostId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  private QuestionPost questionPost;

  // 답변 내용
  @Lob
  @Column(nullable = false)
  private String content;

  // 좋아요 수 (추천 수)
  @Builder.Default
  private Integer likes = 0;

  // 댓글 수
  @Builder.Default
  private Integer commentCount = 0;

  // 답변 채택 여부
  @Builder.Default
  private Boolean isChaetaek = false;
}
