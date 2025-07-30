package com.balsamic.sejongmalsami.post.object.postgres;

import com.balsamic.sejongmalsami.constants.DocumentType;
import com.balsamic.sejongmalsami.constants.PostTier;
import com.balsamic.sejongmalsami.object.postgres.BasePost;
import com.balsamic.sejongmalsami.object.postgres.Member;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
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
public class DocumentPost extends BasePost {

  private static final int MAX_DOCUMENT_TYPES = 2; // 정적 태그 제한 개수

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID documentPostId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member; // 작성자

  private String title; // 제목

  private String subject; // 교과목명

  @Column(nullable = false, length = 1024)
  private String content; // 내용

  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private List<String> faculties = new ArrayList<>(); // 교과목명에 해당하는 단과대

  @Builder.Default
  @ElementCollection(targetClass = DocumentType.class, fetch = FetchType.LAZY)
  @CollectionTable
  @Enumerated(EnumType.STRING)
  @NotNull
  private List<DocumentType> documentTypes = new ArrayList<>();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private PostTier postTier = PostTier.CHEONMIN; // 게시물 등급

  // 파일 미리보기 이미지 URL
  private String thumbnailUrl;

  private Integer attendedYear;

  @Builder.Default
  private Integer dislikeCount = 0; // 싫어요수

  @Builder.Default
  private Boolean isDepartmentPrivate = false; // 내 학과 비공개

  @Builder.Default
  private Boolean isPopular = false; //2024.11.26 : #442 : 인기 게시글 여부 추가

  // 일간 인기글 점수
  @Builder.Default
  private Long dailyScore = 0L;

  // 주간 인기글 점수
  @Builder.Default
  private Long weeklyScore = 0L;

  // 좋아요 누른 글 여부
  @Transient
  @Builder.Default
  private Boolean isLiked = false;

  // 커스텀 태그: DB 저장X
  @Transient
  private List<String> customTags = new ArrayList<>();

  public void increaseDislikeCount() {
    this.dislikeCount++;
  }

  public void decreaseDislikeCount() {
    this.dislikeCount--;
  }
}
