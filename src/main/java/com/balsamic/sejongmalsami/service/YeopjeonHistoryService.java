package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.YeopjeonDto;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.mongo.YeopjeonHistoryRepository;
import com.balsamic.sejongmalsami.util.YeopjeonCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class YeopjeonHistoryService {

  private final YeopjeonHistoryRepository yeopjeonHistoryRepository;
  private final YeopjeonCalculator yeopjeonCalculator;
  private final YeopjeonService yeopjeonService;

  // 엽전 히스토리 내역 추가
  @Transactional
  public YeopjeonDto saveYeopjeonHistory(Member member, YeopjeonAction action) {

    return YeopjeonDto.builder()
        .yeopjeonHistory(yeopjeonHistoryRepository.save(YeopjeonHistory.builder()
            .memberId(member.getMemberId())
            .yeopjeonChange(yeopjeonCalculator.calculateYeopjeon(action))
            .yeopjeonAction(action)
            .resultYeopjeon(yeopjeonService.getResultYeopjeon(member)
                .getYeopjeon().getResultYeopjeon())
            .build()))
        .build();
  }

  // 엽전 히스토리 내역 삭제
  @Transactional
  public void deleteYeopjeonHistory(YeopjeonHistory yeopjeonHistory) {

    yeopjeonHistoryRepository.delete(yeopjeonHistory);
  }

}
