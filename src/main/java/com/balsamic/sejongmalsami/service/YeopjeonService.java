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

  // 엽전 변동 로직 및 엽전 히스토리 내역 저장 (롤백 포함)
  @Transactional
  public YeopjeonHistory updateYeopjeonAndSaveYeopjeonHistory(Member member, YeopjeonAction action) {
    YeopjeonHistory yeopjeonHistory;

    updateMemberYeopjeon(member, action, null);

    try {
      yeopjeonHistory = yeopjeonHistoryService.saveYeopjeonHistory(member, action);
      log.info("회원: {} 엽전 히스토리 저장 성공", member.getStudentId());
    } catch (Exception e) {
      log.error("회원: {} 엽전 히스토리 저장 시 오류가 발생했습니다. 오류내용: {}", member.getStudentId(), e.getMessage());
      log.info("회원: {} 엽전 롤백을 진행합니다.", member.getStudentId());
      rollbackYeopjeon(member, action, null);
      throw new CustomException(ErrorCode.YEOPJEON_SAVE_ERROR);
    }

    return yeopjeonHistory;
  }

  // 엽전 변동 로직 및 엽전 히스토리 내역 저장 (엽전 현상금 커스텀 가능한 경우)
  @Transactional
  public YeopjeonHistory updateYeopjeonAndSaveYeopjeonHistory(Member member, YeopjeonAction action, Integer rewardYeopjeon) {
    YeopjeonHistory yeopjeonHistory;

    updateMemberYeopjeon(member, action, rewardYeopjeon);

    try {
      yeopjeonHistory = yeopjeonHistoryService.saveYeopjeonHistory(member, action);
      log.info("회원: {} 엽전 히스토리 저장 성공", member.getStudentId());
    } catch (Exception e) {
      log.error("회원: {} 엽전 히스토리 저장 시 오류가 발생했습니다. 오류내용: {}", member.getStudentId(), e.getMessage());
      log.info("회원: {} 엽전 롤백을 진행합니다.", member.getStudentId());
      rollbackYeopjeon(member, action, rewardYeopjeon);
      throw new CustomException(ErrorCode.YEOPJEON_SAVE_ERROR);
    }

    return yeopjeonHistory;
  }

  // 엽전 및 엽전 히스토리 전체 롤백 (다른 메서드에서 문제가 발생했을 시 전체 롤백을 위한 메서드)
  @Transactional
  public void rollbackYeopjeonAndDeleteYeopjeonHistory(
      Member member,
      YeopjeonAction action,
      YeopjeonHistory yeopjeonHistory) {
    try {
      log.info("회원: {} 엽전 롤백 및 엽전 내역 삭제를 진행합니다.", member.getStudentId());
      yeopjeonHistoryService.deleteYeopjeonHistory(yeopjeonHistory);
      rollbackYeopjeon(member, action, null);
    } catch (Exception e) {
      log.error("회원: {} 엽전 롤백 과정에서 오류가 발생했습니다.", member.getStudentId());
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 엽전 및 엽전 히스토리 전체 롤백 (엽전 현상금 커스텀 가능할 경우)
  @Transactional
  public void rollbackYeopjeonAndDeleteYeopjeonHistory(
      Member member,
      YeopjeonAction action,
      YeopjeonHistory yeopjeonHistory,
      Integer rewardYeopjeon) {
    try {
      log.info("회원: {} 엽전 롤백 및 엽전 내역 삭제를 진행합니다.", member.getStudentId());
      yeopjeonHistoryService.deleteYeopjeonHistory(yeopjeonHistory);
      rollbackYeopjeon(member, action, rewardYeopjeon);
    } catch (Exception e) {
      log.error("회원: {} 엽전 롤백 과정에서 오류가 발생했습니다.", member.getStudentId());
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 엽전 증감 로직
  private void updateMemberYeopjeon(Member member, YeopjeonAction action, Integer rewardYeopjeon) {

    Yeopjeon yeopjeon = findMemberYeopjeon(member);

    // 엽전 개수 변동
    int calculatedYeopjeon;
    if (action.equals(YeopjeonAction.REWARD_YEOPJEON)) {
      calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action, rewardYeopjeon);
    } else {
      calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action);
    }

    if (calculatedYeopjeon < 0) {
      log.error("엽전이 부족합니다. {}의 엽전 개수: {}", member.getStudentId(), yeopjeon.getYeopjeon());
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    } else {
      yeopjeon.updateYeopjeon(calculatedYeopjeon);
      log.info("엽전 개수 변동 완료: {}의 엽전 개수 = {}", member.getStudentId(), yeopjeon.getYeopjeon());
    }

    yeopjeonRepository.save(yeopjeon);
  }

  // 엽전 개수 롤백
  private void rollbackYeopjeon(Member member, YeopjeonAction action, Integer rewardYeopjeon) {

    Yeopjeon yeopjeon = findMemberYeopjeon(member);

    log.info("엽전 수 롤백 전 - 회원: {}, 엽전 수: {}", member.getStudentId(), yeopjeon.getYeopjeon());

    if (action.equals(YeopjeonAction.REWARD_YEOPJEON)) {
      yeopjeon.updateYeopjeon(yeopjeon.getYeopjeon() - yeopjeonCalculator.calculateYeopjeon(action, rewardYeopjeon));
    } else {
      yeopjeon.updateYeopjeon(yeopjeon.getYeopjeon() - yeopjeonCalculator.calculateYeopjeon(action));
    }

    log.info("엽전 수 롤백 후 - 회원: {}, 롤백 후 엽전 수: {}", member.getStudentId(), yeopjeon.getYeopjeon());
  }

  // 사용자의 엽전 테이블 반환 메서드
  public Yeopjeon findMemberYeopjeon(Member member) {
    return yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
  }

  // 엽전 랭킹 반환
  public int getYeopjeonRank(Member member) {
    return yeopjeonRepository.findRankByMemberId(member.getMemberId());
  }

  public int getTotalYeopjeon() {
    //TODO: Member Total Count 와 yeopjeon Total Count 비교 후 예외처리 (Warning 필요)
//    memberRepository.findTotalMemberCount();
    return yeopjeonRepository.findTotalYeopjeon(); // 전체 개수 가져오기
  }
}
