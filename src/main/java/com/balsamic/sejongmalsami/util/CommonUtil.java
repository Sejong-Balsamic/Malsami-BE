package com.balsamic.sejongmalsami.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
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

  /**
   * 문자열의 SHA-256 해시를 계산합니다.
   *
   * @param input 입력 문자열
   * @return 해시값 문자열
   */
  public static String calculateSha256ByStr(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("SHA-256 해시 계산 실패", e);
    }
  }

  /**
   * float[] 배열 -> 문자열로 변환
   * 앞 뒤에 대괄호와 사이사이 "," 붙임
   * PG에서 vector 사용시 해당 형식 준수 필요
   */
  public static String floatArrayToString(float[] array) {
    if (array == null || array.length == 0) {
      return "[]";
    }
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < array.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(array[i]);
    }
    sb.append("]");
    return sb.toString();
  }

  /**
   * "[0.0, 0.2, ... ]"의 문자열 -> float[] 배열로 변환
   */
  public static float[] stringToFloatArray(String str) {
    if (str == null || str.isEmpty() || str.equals("[]")) {
      return new float[0];
    }
    String cleaned = str.substring(1, str.length() - 1);
    String[] stringArray = cleaned.split(",");
    float[] floatArray = new float[stringArray.length];
    for (int i = 0; i < stringArray.length; i++) {
      floatArray[i] = Float.parseFloat(stringArray[i].trim());
    }
    return floatArray;
  }
}

