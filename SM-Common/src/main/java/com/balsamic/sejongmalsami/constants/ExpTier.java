package com.balsamic.sejongmalsami.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExpTier {
  A(8500, 10000),
  B(8000, 8500),
  C(7500, 8000),
  D(7000, 7500),
  E(6500, 7000),
  F(6000, 6500),
  G(5500, 6000),
  H(5000, 5500),
  I(4500, 5000),
  J(4000, 4500),
  K(3500, 4000),
  L(3000, 3500),
  M(2500, 3000),
  N(2000, 2500),
  O(1500, 2000),
  P(1000, 1500),
  Q(500, 1000),
  R(0, 500);

  private final int minExp;
  private final int maxExp;

  /**
   * 현재 경험치에 해당하는 레벨을 반환합니다.
   */
  public static ExpTier getTierByExp(int exp) {
    for (ExpTier expTier : ExpTier.values()) {
      if (exp >= expTier.getMinExp() && exp < expTier.getMaxExp()) {
        return expTier;
      }
    }
    return R; // 최소 레벨
  }
}
