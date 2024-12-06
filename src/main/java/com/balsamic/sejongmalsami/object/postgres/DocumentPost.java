package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
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
import jakarta.persistence.ManyToOne;
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

  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private List<String> faculties = new ArrayList<>(); // 교과목명에 해당하는 단과대

  private String content; // 내용

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
  private Integer likeCount = 0; // 좋아요수

  @Builder.Default
  private Integer dislikeCount = 0; // 싫어요수

  @Builder.Default
  private Integer commentCount = 0; // 댓글수

  @Builder.Default
  private Integer viewCount = 0; // 조회수

  @Builder.Default
  private Boolean isDepartmentPrivate = false; // 내 학과 비공개

  @Builder.Default
  private Boolean isPopular = false; //2024.11.26 : #442 : 인기 게시글 여부 추가

  public void updateDocumentTypeSet(List<DocumentType> documentTypes) {
    if (documentTypes.size() > MAX_DOCUMENT_TYPES) {
      throw new CustomException(ErrorCode.DOCUMENT_TYPE_LIMIT_EXCEEDED);
    }
    this.documentTypes = documentTypes;
  }

  // 조회 수 증가
  public void increaseViewCount() {
    viewCount++;
  }

  // 댓글 수 증가
  public void increaseCommentCount() {
    commentCount++;
  }

  // 좋아요 수 증가
  public void increaseLikeCount() {
    likeCount++;
  }

  // 좋아요 수 롤백
  public void decreaseLikeCount() {
    likeCount--;
  }

  // 싫어요 수 증가
  public void increaseDislikeCount() {
    dislikeCount++;
  }

  // 싫어요 수 롤백
  public void decreaseDislikeCount() {
    dislikeCount--;
  }
}
