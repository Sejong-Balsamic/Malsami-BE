package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.mongo.ExpHistory;
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
  public ExpHistory updateExpAndSaveExpHistory(Member member, ExpAction action) {
    ExpHistory expHistory;

    updateMemberExp(member, action);

    try {
      expHistory = expHistoryService.saveExpHistory(member, action);
      log.info("회원: {} 경험치 히스토리 저장 성공", member.getStudentId());
    } catch (Exception e) {
      log.error("회원: {} 경험치 히스토리 저장 시 오류가 발생했습니다. 오류내용: {}", member.getStudentId(), e.getMessage());
      log.info("회원: {} 경험치 롤백을 진행합니다.", member.getStudentId());
      rollbackExp(member, action);
      throw new CustomException(ErrorCode.EXP_SAVE_ERROR);
    }

    return expHistory;
  }

  // 경험치 및 경험치 히스토리백 전체 롤백 (다른 메서드에서 문제가 발생했을 시 전체 롤백을 위한 메서드)
  @Transactional
  public void rollbackExpAndDeleteExpHistory(
      Member member,
      ExpAction action,
      ExpHistory expHistory) {
    try {
      log.info("회원: {} 경험치 롤백 및 경험치 내역 삭제를 진행합니다.", member.getStudentId());
      expHistoryService.deleteExpHistory(expHistory);
      rollbackExp(member, action);
    } catch (Exception e) {
      log.error("회원: {} 경험치 롤백 과정에서 오류가 발생했습니다.", member.getStudentId());
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 경험치 증가 로직
  private void updateMemberExp(Member member, ExpAction action) {

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
  private void rollbackExp(Member member, ExpAction action) {

    Exp exp = findMemberExp(member);

    log.info("경험치 롤백 전 - 회원: {}, 경험치: {}", member.getStudentId(), exp.getExp());
    exp.updateExp(exp.getExp() - expCalculator.calculateExp(action));
    log.info("경험치 롤백 후 - 회원: {}, 경험치: {}", member.getStudentId(), exp.getExp());
  }

  // 사용자의 경험치 테이블 반환 메서드
  public Exp findMemberExp(Member member) {
    return expRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.EXP_NOT_FOUND));
  }

  // 경험치 랭킹 반환
  public int getExpRank(Member member) {
    return expRepository.findRankByMemberId(member.getMemberId());
  }

  // 전체 경험치 수 반환
  public int getCountOfMembersWithExp() {
    return expRepository.findExpHolderCount();
  }
}
