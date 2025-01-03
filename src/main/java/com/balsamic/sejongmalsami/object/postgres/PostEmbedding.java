package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.DefaultValue;
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

  // Embedding 벡터값
  @Column(columnDefinition = "TEXT", nullable = false)
  private String embedding;

  // 게시글 카테고리 (자료, 질문)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ContentType contentType;

  private DefaultValue defaultValue;
}
