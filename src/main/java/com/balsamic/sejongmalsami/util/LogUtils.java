package com.balsamic.sejongmalsami.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * 다양한 자료형 -> JSON 형식 가시성 있게 로그 출력
   * 사용예시 : LogUtils.superLog(객체)
   */
  public static void superLog(Object obj) {
    if (obj == null) {
      log.info("\n=============== NULL OBJECT ===============\nObject is null\n==========================================\n");
      return;
    }

    String className = obj.getClass().getSimpleName();
    log.info("\n=============== {} ===============", className);

    try {
      // 객체 -> JSON 변환
      String json = objectMapper.writeValueAsString(obj);
      log.info("\n{}\n", json);
    } catch (JsonProcessingException e) {
      log.error("아닛!? JSON 변환 실패 !!: {}", e.getMessage());
      log.info("대신 toString() 사용~!! : {}", obj.toString());
    }

    log.info("==========================================\n");
  }
}
