package com.balsamic.sejongmalsami.repository.postgres;

import static com.balsamic.sejongmalsami.util.LogUtils.lineLog;
import static com.balsamic.sejongmalsami.util.LogUtils.superLog;

import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.util.TestDataGenerator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
@Transactional
class YeopjeonRepositoryTest {
  @Autowired
  YeopjeonRepository yeopjeonRepository;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @Test
  public void mainTest() {
//    전체엽전개수();
    엽전순위();
  }

  void 엽전순위() {
    Member mockMember1 = testDataGenerator.createMockMember();
    Member mockMember2 = testDataGenerator.createMockMember();
    Member mockMember3 = testDataGenerator.createMockMember();

    lineLog(null);
    Yeopjeon yeopjeon1 = yeopjeonRepository.findByMember(mockMember1)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
    superLog(yeopjeon1);
    lineLog(null);

    lineLog(null);
    Yeopjeon yeopjeon2 = yeopjeonRepository.findByMember(mockMember2)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
    superLog(yeopjeon2);
    lineLog(null);

    lineLog(null);
    Yeopjeon yeopjeon3 = yeopjeonRepository.findByMember(mockMember3)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
    superLog(yeopjeon3);
    lineLog(null);

    lineLog(null);
    Integer rank1 = yeopjeonRepository.findRankByMemberId(mockMember1.getMemberId());
    Integer rank2 = yeopjeonRepository.findRankByMemberId(mockMember2.getMemberId());
    Integer rank3 = yeopjeonRepository.findRankByMemberId(mockMember3.getMemberId());

    lineLog("yeopjeon1 rank");
    superLog(rank1);
    superLog(rank2);
    superLog(rank3);
    System.out.println(yeopjeon1.getYeopjeon());
    System.out.println(yeopjeon2.getYeopjeon());
    System.out.println(yeopjeon3.getYeopjeon());
  }

  void 전체엽전개수() {
    testDataGenerator.createMockMember();
    testDataGenerator.createMockMember();
    testDataGenerator.createMockMember();

    Integer totalMemberCount = yeopjeonRepository.findTotalYeopjeonCount();
    superLog(totalMemberCount);
  }
}