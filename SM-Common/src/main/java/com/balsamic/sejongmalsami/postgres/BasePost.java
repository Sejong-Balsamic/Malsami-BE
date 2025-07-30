package com.balsamic.sejongmalsami.postgres;

import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@ToString
@SuperBuilder
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BasePost extends BaseEntity{

  // 좋아요
  @Builder.Default
  private Integer likeCount = 0;

  // 조회수
  @Builder.Default
  private Integer viewCount = 0;

  // 댓글수
  @Builder.Default
  private Integer commentCount = 0;

  // 닉네임 비공개 여부
  @Builder.Default
  private Boolean isPrivate = false;

  // 공통 메서드
  public void increaseLikeCount() {
    likeCount++;
  }

  public void decreaseLikeCount() {
    if (likeCount <= 0) {
      throw new CustomException(ErrorCode.LIKE_COUNT_CANNOT_BE_NEGATIVE);
    }
    likeCount--;
  }

  public void increaseViewCount() {
    viewCount++;
  }

  public void increaseCommentCount() {
    commentCount++;
  }
}
