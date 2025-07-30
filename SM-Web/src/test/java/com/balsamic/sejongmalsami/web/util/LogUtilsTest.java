package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.util.log.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class LogUtilsTest {

  @Test
  public void mainTest() {
    testLog();
  }

  void testLog() {
    String testMessage = "테스트 메시지";
    LogUtil.lineLog(testMessage);
  }
}