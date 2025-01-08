package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import java.util.ArrayList;
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
  @Column(nullable = false, length = 2048)
  private String content;

  // 과목 명
  @Column(nullable = false)
  private String subject;

  // 단과대
  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private List<String> faculties = new ArrayList<>();

  // 정적 태그
  @ElementCollection(targetClass = QuestionPresetTag.class, fetch = FetchType.LAZY)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "question_preset_tags", joinColumns = @JoinColumn(name = "question_post_id"))
  @Builder.Default
  @Column
  private List<QuestionPresetTag> questionPresetTags = new ArrayList<>();

  // 썸네일
  private String thumbnailUrl;

  // 답변 수
  @Builder.Default
  private Integer answerCount = 0;

  // 엽전 현상금
  @Builder.Default
  private Integer rewardYeopjeon = 0;

  // 게시글 답변 채택 여부
  @Builder.Default
  private Boolean chaetaekStatus = false;

  // 일간 인기글 점수
  @Builder.Default
  private Long dailyScore = 0L;

  // 주간 인기글 점수
  @Builder.Default
  private Long weeklyScore = 0L;

  // 커스텀 태그: 임시 필드: DB에 해당값 저장되지않음
  @Transient
  private List<String> customTags = new ArrayList<>();

  // 질문글 정적 태그 추가(최대 2개)
  public void addPresetTag(QuestionPresetTag tag) {

    if (questionPresetTags.size() >= MAX_PRESET_TAGS) {
      throw new CustomException(ErrorCode.QUESTION_PRESET_TAG_LIMIT_EXCEEDED);
    }

    // 태그 추가
    questionPresetTags.add(tag);
  }
}
