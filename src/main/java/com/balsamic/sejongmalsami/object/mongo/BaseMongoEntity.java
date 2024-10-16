package com.balsamic.sejongmalsami.object.mongo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * MongoDB 공통 필드를 관리
 */
@SuperBuilder
@Getter
@NoArgsConstructor
public abstract class BaseMongoEntity {
  // 생성일
  @CreatedDate
  private LocalDateTime createdDate;

  // 수정일
  @LastModifiedDate
  private LocalDateTime updatedDate;

  // 수정 여부
  @Builder.Default
  private Boolean isEdited = false;
}
