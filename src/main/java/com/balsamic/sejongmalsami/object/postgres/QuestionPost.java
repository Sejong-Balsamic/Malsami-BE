package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.util.HashSet;
import java.util.Set;
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
public class QuestionPost extends BasePost {

  private static final int MAX_PRESET_TAGS = 2;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID questionPostId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  // 제목
  @Column(nullable = false)
  private String title;

  // 본문
  @Lob
  @Column(nullable = false)
  private String content;

  // 과목 명
  @Column(nullable = false)
  private String subject;

  // 정적 태그
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Set<QuestionPresetTag> questionPresetTagSet = new HashSet<>();

  // 조회 수
  @Builder.Default
  private Integer viewCount = 0;

  // 좋아요 수
  @Builder.Default
  private Integer likeCount = 0;

  // 답변 수
  @Builder.Default
  private Integer answerCount = 0;

  // 댓글 수
  @Builder.Default
  private Integer commentCount = 0;

  // 엽전 현상금
  @Builder.Default
  private Integer rewardYeopjeon = 0;

  // 내 정보 비공개 여부
  @Builder.Default
  private Boolean isPrivate = false;

  // 좋아요 증가
  public void increaseLikeCount() {
    likeCount++;
  }

  // 좋아요 감소 (롤백)
  public void decreaseLikeCount() {
    if (likeCount <= 0) {
      throw new CustomException(ErrorCode.LIKE_COUNT_CANNOT_BE_NEGATIVE);
    }
    likeCount--;
  }

  // 질문글 정적 태그 추가(최대 2개)
  public void addPresetTag(QuestionPresetTag tag) {

    if (questionPresetTagSet.size() >= MAX_PRESET_TAGS) {
      throw new CustomException(ErrorCode.QUESTION_PRESET_TAG_LIMIT_EXCEEDED);
    }

    // 태그 추가
    questionPresetTagSet.add(tag);
  }
}
