package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.MimeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Setter // 비밀번호를 설정하고 가져오기 위해 Setter 추가
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true, exclude = "password") // 보안을 위해 비밀번호는 제외
public class DocumentFile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID documentFileId;

  @ManyToOne
  private DocumentPost documentPost;

  @ManyToOne
  private Member uploader;

  // 썸네일 URL
  private String thumbnailUrl;

  // 파일 URL
  private String filePath;

  // 기존 파일
  private String originalFileName;

  // 업로드한 파일 이름
  private String uploadedFileName;

  // 파일 크기
  private Long fileSize;

  @Enumerated(EnumType.STRING)
  private MimeType mimeType;

  @Builder.Default
  private Long downloadCount = 0L;

  private String password;

  // 이미 비밀번호가 설정되어 있는 파일인지
  @Builder.Default
  private Boolean isInitialPasswordSet = false;
}
