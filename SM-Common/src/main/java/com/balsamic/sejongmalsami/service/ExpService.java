package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.constants.ExpAction;
import com.balsamic.sejongmalsami.mongo.ExpHistory;
import com.balsamic.sejongmalsami.postgres.Exp;
import com.balsamic.sejongmalsami.postgres.Member;
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


  /**
   * <h3>경험치 처리 수행 메서드</h3>
   * <p>calculateAndValidateExp 메소드를 통해 ExpAction에 따른 경험치 변동량 계싼</p>
   * <p>applyExp 메소드를 통해 사용자 경험치 변동 적용</p>
   *
   * @param member 경험치 변동 회원
   * @param action 경험치 액션
   * @return 저장된 경험치 히스토리 내역
   */
  @Transactional
  public ExpHistory processExp(Member member, ExpAction action) {

    // 경험치 계산 및 검증
    int newExp = calculateAndValidateExp(member, action, false);

    // 경험치 변동 적용
    applyExpChange(member, newExp);

    // 경험치 히스토리 저장
    ExpHistory expHistory;
    try {
      expHistory = expHistoryService.saveExpHistory(member, action);
      log.info("회원: {} 경험치 히스토리 저장 성공", member.getStudentId());
    } catch (Exception e) {
      log.error("회원: {} 경험치 히스토리 저장 중 오류 발생: {}", member.getStudentId(), e.getMessage());
      int rollbackExp = calculateAndValidateExp(member, action, true);
      applyExpChange(member, rollbackExp);
      throw new CustomException(ErrorCode.EXP_SAVE_ERROR);
    }

    return expHistory;
  }

  // 경험치 및 경험치 히스토리백 전체 롤백 (다른 메서드에서 문제가 발생했을 시 전체 롤백을 위한 메서드)

  /**
   * <h3>경험치 트랜잭션을 롤백하고 히스토리를 삭제하는 메서드</h3>
   * <p>calculateAndValidateExp 메소드를 통해 ExpAction 따른 경험치 변동량 계산 </p>
   * <p>applyExp 메소드를 통해 사용자 경험치 변동 적용</p>
   * <p>경험치 히스토리 삭제</p>
   *
   * @param member 롤백할 사용자
   * @param action 경험치 액션
   * @param history 롤백할 경험치 히스토리 내역
   */
  @Transactional
  public void rollbackExpTransaction(Member member, ExpAction action, ExpHistory history) {

    // 경험치 계산 및 검증
    int rollbackExp = calculateAndValidateExp(member, action, true);

    // 경험치 변동 적용
    applyExpChange(member, rollbackExp);

    try {
      // 경험치 히스토리 삭제
      expHistoryService.deleteExpHistory(history);
      log.info("ExpHistory 삭제 성공: HistoryId = {}", history.getExpHistoryId());
    } catch (Exception e) {
      // 경험치 롤백 취소
      int newExp = calculateAndValidateExp(member, action, false);
      applyExpChange(member, newExp);
      log.error("경험치 변경 롤백 중 오류 발생", e.getMessage());
      throw new CustomException(ErrorCode.EXP_ROLLBACK_ERROR);
    }
  }

  /**
   * <h3>경험치 계산 및 검증</h3>
   * <p>Member, ExpAction을 파라미터로 받아 해당 경험치 액션이 적용가능한지 여부를 판단합니다.</p>
   * <p>롤백 시 isRollback = true 요청</p>
   *
   * @param member 경험치 변동 회원
   * @param action 경험치 액션
   * @param isRollback 롤백 여부 (롤백 시 true 요청)
   * @return
   */
  private int calculateAndValidateExp(Member member, ExpAction action, Boolean isRollback) {
    Exp exp = findMemberExp(member);
    int calculatedExp;

    if (!isRollback) { // 요청 시
      calculatedExp = exp.getExp() + expCalculator.calculateExp(action);
    } else { // 롤백 시
      calculatedExp = exp.getExp() - expCalculator.calculateExp(action);
    }

    if (calculatedExp < 0) {
      log.error("경험치 부족: 회원 {}의 현재 경험치: {}", member.getStudentId(), exp.getExp());
      throw new CustomException(ErrorCode.INSUFFICIENT_EXP);
    }

    return calculatedExp;
  }

  // 경험치 변동 적용
  private void applyExpChange(Member member, int newExp) {
    Exp exp = findMemberExp(member);
    exp.updateExp(newExp);
    expRepository.save(exp);
    log.info("회원 {}의 경험치 변경 완료: 새로운 경험치 = {}", member.getStudentId(), newExp);
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
