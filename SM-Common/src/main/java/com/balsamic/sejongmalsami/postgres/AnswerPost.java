package com.balsamic.sejongmalsami.postgres;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.List;
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
public class AnswerPost extends BasePost {

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

  // 답변 채택 여부
  @Builder.Default
  private Boolean isChaetaek = false;

  // 답변 첨부파일
  @Transient
  private List<MediaFile> mediaFiles;

  // 좋아요 누른 글 여부
  @Transient
  @Builder.Default
  private Boolean isLiked = false;

  // 답변 채택
  public void markAsChaetaek() {
    isChaetaek = true;
  }

  // 답변 채택 rollback
  public void rollbackChaetaek() {
    isChaetaek = false;
  }
}
