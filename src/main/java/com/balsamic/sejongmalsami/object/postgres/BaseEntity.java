package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@ToString
@SuperBuilder
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  // 작성일
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdDate;

  // 수정일
  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedDate;

  // 수정여부
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