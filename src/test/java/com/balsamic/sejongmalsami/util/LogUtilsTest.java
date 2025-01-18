//package com.balsamic.sejongmalsami.util;
//
//import com.balsamic.sejongmalsami.object.postgres.Member;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//@Slf4j
//class LogUtilsTest {
//
//  @Test
//  public void mainTest() {
//    testLog();
//  }
//
//  void testLog() {
//    String testMessage = "테스트 메시지";
//
//    for (int i = 0; i < 6; i++) {
//      LogUtils.lineLog(i+"번쨰"+testMessage);
//    }
//
//    LogUtils.superLog(testMessage);
//    // 빈 "=" 줄 출력
//    log.info("본문 내용");
//    LogUtils.lineLog(null);
//
//    LogUtils.superLog(
//        Member.builder()
//        .studentName("테스트이름 헬로")
//        .build());
//
//  }
//}