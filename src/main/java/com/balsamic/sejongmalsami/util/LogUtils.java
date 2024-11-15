package com.balsamic.sejongmalsami.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그 유틸리티 클래스
 */
@Slf4j
public class LogUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .enable(SerializationFeature.INDENT_OUTPUT);

  private static final int LINE_LENGTH = 60; // "=" 줄에 대한 최대 길이 지정
  private static final String SEPARATOR_CHAR = "=";

  /**
   * 로그 레벨을 정의
   */
  public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
  }

  public static void superLog(Object obj) {
    superLogImpl(obj, LogLevel.INFO);
  }
  public static void superLogDebug(Object obj) {
    superLogImpl(obj, LogLevel.DEBUG);
  }
  public static void superLogWarn(Object obj) {
    superLogImpl(obj, LogLevel.WARN);
  }

  public static void superLogError(Object obj) {
    superLogImpl(obj, LogLevel.ERROR);
  }

  /**
   * 다양한 자료형을 지정된 로그 레벨로 JSON 형식으로 가시성 있게 로그 출력
   * @param obj   로그로 출력할 객체
   * @param level 로그 레벨
   */
  private static void superLogImpl(Object obj, LogLevel level) {
    if (obj == null) {
      lineLogImpl("NULL OBJECT", level);
      logAtLevel(level, "Object is null");
      lineLogImpl(null, level);
      return;
    }

    String className = obj.getClass().getSimpleName();
    lineLogImpl(className, level);

    try {
      // 객체를 JSON 문자열로 변환
      String json = objectMapper.writeValueAsString(obj);
      logAtLevel(level, "{}", json);
    } catch (JsonProcessingException e) {
      logAtLevel(LogLevel.ERROR, "아닛!? JSON 변환 실패 !!: {}", e.getMessage());
      logAtLevel(level, "대신 toString() 사용~!! : {}", obj.toString());
    }

    lineLogImpl(null, level);
  }

  /**
   * ======== 라인 출력 로그 메소드 ==========
   */
  public static void lineLog(String title) {
    lineLogImpl(title, LogLevel.INFO);
  }
  public static void lineLogDebug(String title) {
    lineLogImpl(title, LogLevel.DEBUG);
  }
  public static void lineLogWarn(String title) {
    lineLogImpl(title, LogLevel.WARN);
  }
  public static void lineLogError(String title) {
    lineLogImpl(title, LogLevel.ERROR);
  }

  /**
   * 다양한 로그 레벨로 제목이 중앙에 포함된 구분선을 로그에 출력합니다.
   */
  private static void lineLogImpl(String title, LogLevel level) {
    String separator;
    if (title == null || title.isEmpty()) {
      separator = SEPARATOR_CHAR.repeat(LINE_LENGTH);
    } else {
      int textLength = title.length() + 2; // 양쪽 공백 포함
      if (textLength >= LINE_LENGTH) {
        // 제목이 너무 길 경우 전체 구분선을 제목으로 대체
        separator = title;
      } else {
        int sideLength = (LINE_LENGTH - textLength) / 2;
        String side = SEPARATOR_CHAR.repeat(sideLength);
        separator = side + " " + title + " " + side;

        // 홀수 길이 조정을 위해 추가 '='
        if (separator.length() < LINE_LENGTH) {
          separator += SEPARATOR_CHAR;
        }
      }
    }
    logAtLevel(level, "{}", separator);
  }

  /**
   * 지정된 로그 레벨에 따라 로그를 출력합니다.
   */
  private static void logAtLevel(LogLevel level, String message, Object... args) {
    switch (level) {
      case DEBUG:
        log.debug(message, args);
        break;
      case INFO:
        log.info(message, args);
        break;
      case WARN:
        log.warn(message, args);
        break;
      case ERROR:
        log.error(message, args);
        break;
      default:
        log.info(message, args);
    }
  }
}
