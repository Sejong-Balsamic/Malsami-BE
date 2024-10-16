package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
public class DocumentPost extends BasePost {

  private static final int MAX_DOCUMENT_TYPES = 2;

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
  private Set<DocumentType> documentTypeSet = new HashSet<>();

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private PostTier postTier = PostTier.CHEONMIN; // 게시물 등급

  // 파일 미리보기 이미지 URL
  private String previewUrl;

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

  // 자료글 카테고리 추가(최대 2개)
  public void addDocumentType(DocumentType documentType) {

    if (documentTypeSet.size() >= MAX_DOCUMENT_TYPES) {
      throw new CustomException(ErrorCode.DOCUMENT_TYPE_LIMIT_EXCEEDED);
    }

    // 카테고리 추가
    documentTypeSet.add(documentType);
  }
}
