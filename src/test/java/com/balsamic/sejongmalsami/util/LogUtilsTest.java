package com.balsamic.sejongmalsami.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LogUtilsTest {

  @Test
  public void mainTest() {
    testLog();
  }

  void testLog() {
    String testMessage = "테스트 메시지";
    for (int i = 0; i < 6; i++) {
      LogUtils.lineLog(testMessage);
    }

    LogUtils.superLog(testMessage);

  }

}