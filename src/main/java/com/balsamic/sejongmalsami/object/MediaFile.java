package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
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

  //TODO: 로직 추가하기
  private String originalFileName;

  // 파일 경로 (파일 URL)
  @Column(length = 1024)
  private String fileUrl;

  // 파일 크기
  private Long fileSize;

  @Enumerated(EnumType.STRING)
  private ContentType contentType;

  @Enumerated(EnumType.STRING)
  private MimeType mimeType;
}
