package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
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
public class DocumentRequestPost extends BaseEntity {

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

  // 자료 타입
  @Builder.Default
  @Enumerated(EnumType.STRING)
  private Set<DocumentType> documentTypeSet = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  private Course course;

  // 내용
  @Lob
  private String content;

  // 닉네임 비공개
  @Builder.Default
  private boolean isPrivate = false;

  // 자료게시글 자료 종류 추가(최대 2개)
  public void addDocumnetType(DocumentType type) {

    if (documentTypeSet.size() >= MAX_DOCUMENT_TYPES) {
      throw new CustomException(ErrorCode.DOCUMENT_TYPE_LIMIT_EXCEEDED);
    }

    // 종류 추가
    documentTypeSet.add(type);
  }
}
