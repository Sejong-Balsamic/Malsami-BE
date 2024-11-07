package com.balsamic.sejongmalsami.object.postgres;

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
  private Member member;

  // 총 경험치량
  private Integer resultExp;

  public void updateResultExp(int resultExp) {
    this.resultExp = resultExp;
  }
}
