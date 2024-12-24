package com.balsamic.sejongmalsami;

import java.util.UUID;

public class CommonUtil {

  public static <T> T nullIfBlank(T value) {

    // 값이 null인 경우 바로 null 반환
    if (value == null) {
      return null;
    }

    // 공백 문자열은 null로 변환
    if (value instanceof String) {
      String str = (String) value;
      return str.trim().isEmpty() ? null : value;
    }

    // UUID의 문자열 표현이 공백인 경우 null 반환
    if (value instanceof UUID) {
      UUID uuid = (UUID) value;
      return uuid.toString().trim().isEmpty() ? null : value;
    }

    // Long이 0이면 null로 변환
    if (value instanceof Long) {
      Long num = (Long) value;
      return num == 0L ? null : value;
    }

    return value; // 다른 타입은 그대로 반환
  }

  public static UUID toUUID(String uuidString) {
    if (uuidString == null || uuidString.trim().isEmpty()) {
      return null; // 빈 값은 null로 반환
    }

    try {
      return UUID.fromString(uuidString); // String을 UUID로 변환
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("잘못된 UUID 형식: " + uuidString, e);
    }
  }
}

