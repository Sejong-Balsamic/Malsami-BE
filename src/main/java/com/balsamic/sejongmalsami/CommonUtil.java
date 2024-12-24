package com.balsamic.sejongmalsami;

public class CommonUtil {
  public static String nullIfBlank(String str) {
    return (str == null || str.trim().isEmpty()) ? null : str;
  }
}
