package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.List;
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
  @ElementCollection
  @Enumerated(EnumType.STRING)
  private Set<DocumentType> documentTypeSet = new HashSet<>();

  // 자료 요청 학과
  @ManyToOne(fetch = FetchType.LAZY)
  private Course requestDepartment;

  // 내용
  @Lob
  private String content;

  // 닉네임 비공개
  @Builder.Default
  private boolean isPrivate = false;
}
