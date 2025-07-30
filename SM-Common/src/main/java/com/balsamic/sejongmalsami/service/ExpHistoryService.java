package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.constants.ExpAction;
import com.balsamic.sejongmalsami.mongo.ExpHistory;
import com.balsamic.sejongmalsami.postgres.Exp;
import com.balsamic.sejongmalsami.postgres.Member;
import com.balsamic.sejongmalsami.repository.mongo.ExpHistoryRepository;
import com.balsamic.sejongmalsami.repository.postgres.ExpRepository;
import com.balsamic.sejongmalsami.util.ExpCalculator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpHistoryService {

  private final ExpRepository expRepository;
  private final ExpHistoryRepository expHistoryRepository;
  private final ExpCalculator expCalculator;

  // 경험치 히스토리 내역 추가
  @Transactional
  public ExpHistory saveExpHistory(Member member, ExpAction action) {

    Exp exp = expRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND));

    ExpHistory expHistory = ExpHistory.builder()
        .memberId(member.getMemberId())
        .expChange(expCalculator.calculateExp(action))
        .expAction(action)
        .resultExp(exp.getExp())
        .build();

    try {
      return expHistoryRepository.save(expHistory);
    } catch (Exception e) {
      log.error("사용자: {}, 경험치 히스토리 저장 실패: {}", member.getStudentId(), e.getMessage());
      throw new CustomException(ErrorCode.EXP_HISTORY_SAVE_ERROR);
    }
  }

  // 경험치 히스토리 내역 삭제
  @Transactional
  public void deleteExpHistory(ExpHistory expHistory) {
    try {
      expHistoryRepository.delete(expHistory);
    } catch (Exception e) {
      log.error("경험치 히스토리 삭제 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.EXP_HISTORY_DELETE_ERROR);
    }
  }
}
