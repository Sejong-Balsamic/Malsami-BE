package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.mongo.ExpHistory;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.mongo.ExpHistoryRepository;
import com.balsamic.sejongmalsami.util.ExpCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpHistoryService {

  private final ExpHistoryRepository expHistoryRepository;
  private final ExpCalculator expCalculator;
  private final ExpService expService;

  // 경험치 히스토리 내역 추가
  @Transactional
  public ExpHistory saveExpHistory(Member member, ExpAction action) {

    Exp exp = expService.findMemberExp(member);

    return ExpHistory.builder()
        .memberId(member.getMemberId())
        .expChange(expCalculator.calculateExp(action))
        .expAction(action)
        .resultExp(exp.getExp())
        .build();
  }

  // 경험치 히스토리 내역 삭제
  @Transactional
  public void deleteExpHistory(ExpHistory expHistory) {
    expHistoryRepository.delete(expHistory);
  }
}
