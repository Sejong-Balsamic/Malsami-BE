package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
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
public class YeopjeonService {

  private final YeopjeonRepository yeopjeonRepository;
  private final YeopjeonCalculator yeopjeonCalculator;
  private final YeopjeonHistoryService yeopjeonHistoryService;
  private final MemberRepository memberRepository;

  // 엽전 처리 수행 메인 메서드 (엽전현상금 없음)
  public YeopjeonHistory processYeopjeon(Member member, YeopjeonAction action) {
    return processYeopjeon(member, action, 0);
  }

  // 엽전 처리 수행 메인 메서드 (엽전현상금 포함)
  @Transactional
  public YeopjeonHistory processYeopjeon(Member member, YeopjeonAction action, Integer rewardYeopjeon) {

    // rewardYeopjeon 기본값 처리
    rewardYeopjeon = (rewardYeopjeon != null) ? rewardYeopjeon : 0;

    // 엽전 계산 및 검증
    int newYeopjeon = calculateAndValidateYeopjeon(member, action, rewardYeopjeon);

    // 엽전 변동 적용
    applyYeopjeonChanges(member, newYeopjeon);

    // 엽전 히스토리 저장
    YeopjeonHistory yeopjeonHistory;
    try {
      yeopjeonHistory = yeopjeonHistoryService.saveYeopjeonHistory(member, action);
      log.info("회원: {} 엽전 히스토리 저장 성공", member.getStudentId());
    } catch (Exception e) {
      log.error("회원: {} 엽전 히스토리 저장 중 오류 발생: {}", member.getStudentId(), e.getMessage());
      revertYeopjeonChange(member, action, rewardYeopjeon);
      throw new CustomException(ErrorCode.YEOPJEON_SAVE_ERROR);
    }

    return yeopjeonHistory;
  }

  // 엽전 계산 및 검증
  private int calculateAndValidateYeopjeon(Member member, YeopjeonAction action, Integer rewardYeopjeon) {
    Yeopjeon yeopjeon = findMemberYeopjeon(member);
    int calculatedYeopjeon;

    if (action.equals(YeopjeonAction.REWARD_YEOPJEON)) {
      calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action, rewardYeopjeon);
    } else {
      calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action);
    }

    if (calculatedYeopjeon < 0) {
      log.error("엽전 부족: 회원 {}의 현재 엽전: {}", member.getStudentId(), yeopjeon.getYeopjeon());
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    }

    return calculatedYeopjeon;
  }

  // 엽전 변동 적용
  private void applyYeopjeonChanges(Member member, int newYeopjeon) {
    Yeopjeon yeopjeon = findMemberYeopjeon(member);
    yeopjeon.setYeopjeon(newYeopjeon);
    yeopjeonRepository.save(yeopjeon);
    log.info("회원 {}의 엽전 변경 완료: 새로운 엽전 개수 = {}", member.getStudentId(), newYeopjeon);
  }

  // 엽전 롤백
  private void revertYeopjeonChange(Member member, YeopjeonAction action, Integer rewardYeopjeon) {
    int rollbackAmount = rewardYeopjeon != null ? -rewardYeopjeon : 0;
    log.info("회원 {}의 엽전 롤백 진행: 롤백 금액 = {}", member.getStudentId(), rollbackAmount);
    Yeopjeon yeopjeon = findMemberYeopjeon(member);
    int rollbackYeopjeon = calculateAndValidateYeopjeon(member, action, rollbackAmount);
    applyYeopjeonChanges(member, rollbackYeopjeon);
  }


  /**
   * 엽전 트랜잭션을 롤백하고 히스토리를 삭제하는 메서드
   *
   * @param member  롤백할 회원
   * @param action  엽전 액션
   * @param history 롤백할 엽전 히스토리
   */
  @Transactional
  public void rollbackYeopjeonTransaction(Member member, YeopjeonAction action, YeopjeonHistory history) {
    try {
      // 엽전 히스토리 삭제
      yeopjeonHistoryService.deleteYeopjeonHistory(history);
      log.info("YeopjeonHistory 삭제 성공: HistoryId = {}", history.getYeopjeonHistoryId());

      // 엽전 변경 되돌리기
      revertYeopjeonChange(member, action, history.getYeopjeonChange());
      log.info("Yeopjeon 변경 되돌리기: studentId = {}", member.getStudentId());
    } catch (Exception e) {
      log.error("Yeopjeon 변경 되돌리기 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.YEOPJEON_ROLLBACK_ERROR);
    }
  }

  // 회원 엽전 정보 조회
  public Yeopjeon findMemberYeopjeon(Member member) {
    return yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
  }

  // 엽전 랭킹 반환
  public int getYeopjeonRank(Member member) {
    return yeopjeonRepository.findRankByMemberId(member.getMemberId());
  }

  public int getCountOfMembersWithYeopjeon() {
    //TODO: Member Total Count 와 yeopjeon Total Count 비교 후 예외처리 (Warning 필요)
//    memberRepository.findTotalMemberCount();
    return yeopjeonRepository.findYeopjeonHolderCount(); // 전체 개수 가져오기
  }

}
