package com.balsamic.sejongmalsami.object.mongo;

import jakarta.persistence.PreUpdate;
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

  // 삭제여부
  @Builder.Default
  private Boolean isDeleted = false;

  // 엔티티 업데이트 시 호출 (수정여부)
  @PreUpdate
  public void beforeUpdate() {
    if (!createdDate.isEqual(updatedDate)) {
      this.isEdited = true;
    }
  }
}
