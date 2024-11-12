package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Comment extends BaseEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID commentId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  // 댓글이 속한 게시글의 ID
  private UUID postId;

  private String content;

  // 좋아요
  @Builder.Default
  private Integer likeCount = 0;

  // 댓글이 속한 게시글의 유형
  @Enumerated(EnumType.STRING)
  private ContentType contentType;

  // 닉네임 비공개 여부
  @Builder.Default
  private Boolean isPrivate = false;

  // 좋아요 증가
  public void increaseLikeCount() {
    likeCount++;
  }

  // 좋아요 감소
  public void rollbackLikeCount() {
    likeCount--;
  }
}
