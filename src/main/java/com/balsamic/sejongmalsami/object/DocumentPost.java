package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.PostTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class DocumentPost extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID documentPostId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member; // 작성자

  private String title; // 제목

  private String subject; // 교과목명

  @Lob
  private String content; // 내용

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private PostTier postTier = PostTier.CHEONMIN; // 게시물 등급

  @Builder.Default
  private Integer likeCount = 0; // 추천수

  @Builder.Default
  private Integer dislikeCount = 0; // 싫어요수

  @Builder.Default
  private Integer downloadCount = 0; // 다운로드수

  @Builder.Default
  private Integer commentCount = 0; // 댓글수

  @Builder.Default
  private Integer viewCount = 0; // 조회수

  @Builder.Default
  private Boolean isDepartmentPrivate = false; // 내 학과 비공개
}
