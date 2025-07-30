package com.balsamic.sejongmalsami.postgres;

import com.balsamic.sejongmalsami.constants.DocumentType;
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
public class DocumentRequestPost extends BasePost {

  private static final int MAX_DOCUMENT_TYPES = 2;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID documentRequestPostId;

  // 작성자
  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  // 제목
  private String title;

  // 내용
  private String content;

  // 교과목명
  private String subject;

  // 단과대
  @ElementCollection(fetch = FetchType.LAZY)
  @Builder.Default
  private List<String> faculties = new ArrayList<>();

  // 자료 타입
  @ElementCollection(targetClass = DocumentType.class, fetch = FetchType.LAZY)
  @Enumerated(EnumType.STRING)
  @CollectionTable
  @Builder.Default
  @Column
  private List<DocumentType> documentTypes = new ArrayList<>();

  // 좋아요 누른 글 여부
  @Transient
  @Builder.Default
  private Boolean isLiked = false;
}
