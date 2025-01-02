package com.balsamic.sejongmalsami.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

  public static final String BASIC_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String FILENAME_DATE_TIME_FORMAT = "yyyyMMdd_HHmmss";
  public static final String DATE_TIME_MILLIS_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  public static final DateTimeFormatter BASIC_DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern(BASIC_DATE_TIME_FORMAT);
  public static final DateTimeFormatter FILENAME_DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern(FILENAME_DATE_TIME_FORMAT);
  public static final DateTimeFormatter DATE_TIME_MILLIS_FORMATTER
      = DateTimeFormatter.ofPattern(DATE_TIME_MILLIS_FORMAT);

  // 기본 현재 시간(초) 반환
  public static String formatLocalDateTimeNow() {
    return BASIC_DATE_TIME_FORMATTER.format(LocalDateTime.now());
  }

  // 기본 현재 시간(미리초) 반환
  public static String formatLocalDateTimeMillisNow() {
    return DATE_TIME_MILLIS_FORMATTER.format(LocalDateTime.now());
  }

  // 현재 시간 반환 (언더바 포맷)
  public static String formatLocalDateTimeNowForFileName() {
    return FILENAME_DATE_TIME_FORMATTER.format(LocalDateTime.now());
  }

  /**
   * ms -> m / s
   * 밀리초를 초/분으로 변환
   * 예) 850ms -> 850미리초
   * 55000ms -> 55초
   * 125000ms -> 2분 5초
   */
  public static String convertMillisToReadableTime(long millis) {
    if (millis < 1000) {
      return millis + "미리초";
    } else if (millis < 60 * 1000) {
      return (millis / 1000) + "초";
    } else {
      long seconds = millis / 1000;
      long minutes = seconds / 60;
      seconds %= 60;
      return minutes + "분 " + seconds + "초";
    }
  }
}
