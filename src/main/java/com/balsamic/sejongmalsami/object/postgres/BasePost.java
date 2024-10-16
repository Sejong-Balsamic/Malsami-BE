package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@ToString
@SuperBuilder
@MappedSuperclass
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BasePost extends BaseEntity{

  // 일간 인기글 점수
  private Integer dailyScore;

  // 주간 인기글 점수
  private Integer weeklyScore;

  public void updateDailyScore(Integer dailyScore) {
    this.dailyScore = dailyScore;
  }

  public void updateWeeklyScore(Integer weeklyScore) {
    this.weeklyScore = weeklyScore;
  }
}
