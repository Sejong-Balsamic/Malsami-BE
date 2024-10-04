package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ExtensionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

  @ManyToOne(fetch = FetchType.LAZY)
  private QuestionPost questionPost;

  @ManyToOne(fetch = FetchType.LAZY)
  private AnswerPost answerPost;

  // 파일 이름
  private String fileName;

  // 파일 경로 (파일 URL)
  private String filePath;

  @Enumerated(EnumType.STRING)
  private ExtensionType fileType;
}
