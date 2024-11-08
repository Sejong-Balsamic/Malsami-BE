package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.YeopjeonHistoryRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.YeopjeonCalculator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class YeopjeonHistoryService {

  private final YeopjeonRepository yeopjeonRepository;
  private final YeopjeonHistoryRepository yeopjeonHistoryRepository;
  private final YeopjeonCalculator yeopjeonCalculator;

  // 엽전 히스토리 내역 추가
  @Transactional
  public YeopjeonHistory saveYeopjeonHistory(Member member, YeopjeonAction action) {

    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));

    return YeopjeonHistory.builder()
        .memberId(member.getMemberId())
        .yeopjeonChange(yeopjeonCalculator.calculateYeopjeon(action))
        .yeopjeonAction(action)
        .resultYeopjeon(yeopjeon.getYeopjeon())
        .build();
  }

  // 엽전 히스토리 내역 삭제
  @Transactional
  public void deleteYeopjeonHistory(YeopjeonHistory yeopjeonHistory) {
    yeopjeonHistoryRepository.delete(yeopjeonHistory);
  }

}
