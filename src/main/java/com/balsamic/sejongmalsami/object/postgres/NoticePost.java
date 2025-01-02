package com.balsamic.sejongmalsami.object.postgres;

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
public class NoticePost extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID noticePostId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  // 제목
  @Column(nullable = false)
  private String title;

  // 본문
  @Column(nullable = false)
  private String content;

  // 조회 수
  @Builder.Default
  private Integer viewCount = 0;

  // 좋아요 수
  @Builder.Default
  private Integer likeCount = 0;

  // 공개/비공개 여부
  @Builder.Default
  private Boolean isHidden = false;
}
