package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
  @Column(nullable = false)
  private String content;

  // 좋아요 수
  @Builder.Default
  private Integer likeCount = 0;

  // 댓글 수
  @Builder.Default
  private Integer commentCount = 0;

  // 답변 채택 여부
  @Builder.Default
  private Boolean isChaetaek = false;

  // 닉네임 비공개 여부
  @Builder.Default
  private Boolean isPrivate = false;

  // 답변 좋아요 증가
  public void increaseLikeCount() {
    likeCount++;
  }

  // 답변 좋아요 감소 (롤백)
  public void decreaseLikeCount() {
    if (likeCount <= 0) {
      throw new CustomException(ErrorCode.LIKE_COUNT_CANNOT_BE_NEGATIVE);
    }
    likeCount--;
  }

  // 답변 채택
  public void chaetaekAnswer() {
    isChaetaek = true;
  }

  // 답변 채택 rollback
  public void rollbackChaetaek() {
    isChaetaek = false;
  }
}
