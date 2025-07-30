package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.postgres.BaseEntity;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.FileStatus;
import com.balsamic.sejongmalsami.util.converter.FloatArrayConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
public class PostEmbedding extends BaseEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID postEmbeddingId;

  @Column(nullable = false)
  private UUID postId;

  // Embedding Vector 값
  @Convert(converter = FloatArrayConverter.class)
  @Column(columnDefinition = "TEXT")
  private float[] embedding;

  // 게시글 카테고리 (DOCUMENT, QUESTION)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentType contentType;

  // 작업 상태: IN_PROGRESS, SUCCESS, FAILURE
  private FileStatus fileStatus;

  // 추가 설명 (목데이터, 에러메시지 등등)
  private String message;
}
