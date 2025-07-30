package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.postgres.BaseEntity;

import com.balsamic.sejongmalsami.util.converter.FloatArrayConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
public class SearchQueryCache {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()")
  private UUID searchQueryCacheId;

  // 검색어
  @Column(nullable = false, unique = true, length = 500)
  private String queryText;

  // embedding vector
  @Convert(converter = FloatArrayConverter.class)
  @Column(columnDefinition = "TEXT")
  private float[] embedding;

  // 검색 횟수
  private long searchCount;
}
