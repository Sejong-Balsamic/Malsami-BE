package com.balsamic.sejongmalsami.object.postgres;

import com.balsamic.sejongmalsami.object.constants.ExpTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Exp {


  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false)
  private UUID ExpId;

  @OneToOne
  private com.balsamic.sejongmalsami.object.postgres.Member member;

  // 총 경험치량
  private Integer exp;

  // 현재 레벨
  private ExpTier expTier;

  // 현재 레벨의 시작 경험치
  private Integer tierStartExp;

  // 현재 레벨의 끝 경험치
  private Integer tierEndExp;

  // 다음 티어까지 진행률 (퍼센트)
  private Double progressPercent;

  public void updateExp(int exp) {
    this.exp = exp;
    updateExpTierInfo();
  }

  private void updateExpTierInfo() {
    ExpTier expTier = ExpTier.getTierByExp(this.exp);
    tierStartExp = expTier.getMinExp();
    tierEndExp = expTier.getMaxExp();

    int expInLevel = exp - tierStartExp;
    int levelRange = tierEndExp - tierStartExp;
    this.progressPercent = (double) expInLevel / levelRange * 100;
  }
}
