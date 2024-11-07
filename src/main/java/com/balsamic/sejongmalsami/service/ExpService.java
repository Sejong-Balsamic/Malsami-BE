package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
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
public class ExpService {

  private final ExpRepository expRepository;
  private final ExpCalculator expCalculator;
  private final ExpHistoryService expHistoryService;

  // 경험치 증가 로직 및 경험치 히스토리 내역 저장 (롤백 포함)
  @Transactional
  public void updateExpAndSaveExpHistory(Member member, ExpAction action) {
    updateMemberExp(member, action);

    try {
      expHistoryService.saveExpHistory(member, action);
      log.info("경험치 히스토리 저장 성공");
    } catch (Exception e) {
      log.error("경험치 히스토리 저장 시 오류가 발생했습니다. 오류내용: {}", e.getMessage());
      log.info("경험치 롤백을 진행합니다.");
      rollbackExp(member, action);
      throw new CustomException(ErrorCode.EXP_SAVE_ERROR);
    }
  }

  // 경험치 증가 로직
  @Transactional
  public void updateMemberExp(Member member, ExpAction action) {

    Exp exp = findMemberExp(member);

    // 경험치 증가
    int calculatedExp = exp.getExp() + expCalculator.calculateExp(action);

    if (calculatedExp < 0) {
      log.error("경험치가 부족합니다. {}의 현재 경험치: {}", member.getStudentId(), exp.getExp());
      throw new CustomException(ErrorCode.INSUFFICIENT_EXP);
    } else {
      exp.updateExp(calculatedExp);
      log.info("경험치 변동 완료. 변동 후 {}의 경험치: {}", member.getStudentId(), exp.getExp());
    }

    expRepository.save(exp);
  }

  // 경험치 롤백
  @Transactional
  public void rollbackExp(Member member, ExpAction action) {

    Exp exp = findMemberExp(member);

    log.info("경험치 롤백 전 - 회원: {}, 경험치: {}", member.getStudentId(), exp.getExp());
    exp.updateExp(exp.getExp()
                  - expCalculator.calculateExp(action));
    log.info("경험치 롤백 후 - 회원: {}, 경험치: {}", member.getStudentId(), exp.getExp());
  }

  // 사용자의 경험치 테이블 반환 메서드
  public Exp findMemberExp(Member member) {
    return expRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND));
  }
}
