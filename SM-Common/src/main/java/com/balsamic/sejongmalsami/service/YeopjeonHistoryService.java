package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.constants.YeopjeonAction;
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

    int yeopjeonAmount = yeopjeonCalculator.calculateYeopjeon(action);
    return saveYeopjeonHistory(member, action, yeopjeonAmount);
  }

  // 엽전 히스토리 저장 (커스텀 엽전 값 처리)
  @Transactional
  public YeopjeonHistory saveYeopjeonHistory(Member member, YeopjeonAction action, Integer yeopjeonAmount) {

    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));

    YeopjeonHistory yeopjeonHistory = YeopjeonHistory.builder()
        .memberId(member.getMemberId())
        .yeopjeonChange(yeopjeonAmount != null ? yeopjeonAmount : yeopjeonCalculator.calculateYeopjeon(action))
        .yeopjeonAction(action)
        .resultYeopjeon(yeopjeon.getYeopjeon())
        .build();

    try {
      return yeopjeonHistoryRepository.save(yeopjeonHistory);
    } catch (Exception e) {
      log.error("사용자: {}, 엽전 히스토리 저장 실패: {}", member.getStudentId(), e.getMessage());
      throw new CustomException(ErrorCode.YEOPJEON_HISTORY_SAVE_ERROR);
    }
  }

  // 엽전 히스토리 내역 삭제
  @Transactional
  public void deleteYeopjeonHistory(YeopjeonHistory yeopjeonHistory) {
    try {
      yeopjeonHistoryRepository.delete(yeopjeonHistory);
    } catch (Exception e) {
      log.error("엽전 히스토리 삭제 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.YEOPJEON_HISTORY_DELETE_ERROR);
    }
  }

}
