package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.postgres.BaseEntity;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.MimeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
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
public class MediaFile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID mediaFileId;

  // 질문 or 답변 PK
  private UUID postId;

  // 파일 원본명
  private String originalFileName;

  // 업로드된 파일명
  private String uploadedFileName;

  // 썸네일 URL
  private String thumbnailUrl;

  // 압축된 이미지 URL (화질좋음)
  private String uploadedImageUrl;

  // 파일 경로
  private String filePath;

  // 파일 크기
  private Long fileSize;

  @Enumerated(EnumType.STRING)
  private ContentType contentType;

  @Enumerated(EnumType.STRING)
  private MimeType mimeType;
}
