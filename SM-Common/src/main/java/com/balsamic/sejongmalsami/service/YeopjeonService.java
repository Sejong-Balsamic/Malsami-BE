package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.constants.YeopjeonAction.*;

import com.balsamic.sejongmalsami.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
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

  /**
   * <h3>엽전 처리 수행 메서드 (엽전현상금 없음)</h3>
   *
   * @param member 엽전 변동 회원
   * @param action 엽전 액션
   * @return 저장된 엽전 히스토리
   */
  @Transactional
  public YeopjeonHistory processYeopjeon(Member member, YeopjeonAction action) {
    return processYeopjeon(member, action, null);
  }

  /**
   * <h3>엽전 처리 수행 메인 메서드 (엽전현상금 포함)</h3>
   * <p>calculateAndValidateYeopjeon 메소드를 통해 YeopjeonAction에 따른 엽전 변동량 계산</p>
   * <p>applyYeopjeon 메소드를 통해 사용자 엽전 변동 적용</p>
   * <p>엽전 히스토리 저장</p>
   * <p>엽전 현상금이 0으로 설정된 경우 엽전 히스토리 저장X</p>
   *
   * @param member         엽전 변동 회원
   * @param action         엽전 액션
   * @param rewardYeopjeon 엽전 현상금 (커스텀)
   * @return 저장된 엽전 히스토리 내역
   */
  @Transactional
  public YeopjeonHistory processYeopjeon(Member member, YeopjeonAction action, Integer rewardYeopjeon) {

    if (rewardYeopjeon != null && rewardYeopjeon == 0) {
      return null;
    }

    // 엽전 계산 및 검증
    int newYeopjeon = calculateAndValidateYeopjeon(member, action, rewardYeopjeon, false);

    // 엽전 변동 적용
    applyYeopjeonChange(member, newYeopjeon);

    // 엽전 히스토리 저장
    YeopjeonHistory yeopjeonHistory;
    try {
      yeopjeonHistory = yeopjeonHistoryService.saveYeopjeonHistory(member, action);
      log.info("회원: {} 엽전 히스토리 저장 성공", member.getStudentId());
    } catch (Exception e) {
      log.error("회원: {} 엽전 히스토리 저장 중 오류 발생: {}", member.getStudentId(), e.getMessage());
      int rollbackYeopjeon = calculateAndValidateYeopjeon(member, action, rewardYeopjeon, true);
      applyYeopjeonChange(member, rollbackYeopjeon);
      throw new CustomException(ErrorCode.YEOPJEON_SAVE_ERROR);
    }

    return yeopjeonHistory;
  }

  /**
   * <h3>엽전 트랜잭션을 롤백하고 히스토리를 삭제하는 메서드 (엽전 현상금 없음)</h3>
   *
   * @param member  롤백할 회원
   * @param action  엽전 액션
   * @param history 롤백할 엽전 히스토리
   */
  @Transactional
  public void rollbackYeopjeonTransaction(Member member, YeopjeonAction action, YeopjeonHistory history) {
    rollbackYeopjeonTransaction(member, action, null, history);
  }

  /**
   * <h3>엽전 트랜잭션을 롤백하고 히스토리를 삭제하는 메인 메서드 (엽전 현상금 포함)</h3>
   * <p>calculateAndValidateYeopjeon 메소드를 통해 YeopjeonAction에 따른 엽전 변동량 계산</p>
   * <p>applyYeopjeon 메소드를 통해 사용자 엽전 변동 적용</p>
   * <p>엽전 히스토리 삭제</p>
   * <p>엽전 현상금이 0인 경우 변동X</p>
   *
   * @param member         롤백할 회원
   * @param action         엽전 액션
   * @param rewardYeopjeon 엽전 현상금 (커스텀)
   * @param history        롤백할 엽전 히스토리 내역
   */
  @Transactional
  public void rollbackYeopjeonTransaction(Member member, YeopjeonAction action, Integer rewardYeopjeon, YeopjeonHistory history) {

    // 엽전 현상금이 0인 경우
    if (rewardYeopjeon == null || rewardYeopjeon == 0) {
      return;
    }

    // 엽전 계산 및 검증
    int rollbackYeopjeon = calculateAndValidateYeopjeon(member, action, rewardYeopjeon, true);

    // 엽전 변동 적용
    applyYeopjeonChange(member, rollbackYeopjeon);

    try {
      // 엽전 히스토리 삭제
      yeopjeonHistoryService.deleteYeopjeonHistory(history);
      log.info("YeopjeonHistory 삭제 성공: HistoryId = {}", history.getYeopjeonHistoryId());
    } catch (Exception e) {
      // 엽전 롤백 취소
      int newYeopjeon = calculateAndValidateYeopjeon(member, action, rewardYeopjeon, false);
      applyYeopjeonChange(member, newYeopjeon);
      log.error("엽전 변경 롤백 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.YEOPJEON_ROLLBACK_ERROR);
    }
  }

  /**
   * <h3>엽전 계산 및 검증</h3>
   * <p>Member, YeopjeonAction을 파라미터로 받아 해당 엽전 액션이 적용가능한지 여부를 판단합니다.</p>
   * <p>롤백 시 isRollback = true 요청</p>
   *
   * @param member         엽전 변동 회원
   * @param action         엽전 액션
   * @param rewardYeopjeon 엽전 현상금 (커스텀)
   * @param isRollback     롤백 여부 (롤백 시 true 요청)
   * @return
   */
  private int calculateAndValidateYeopjeon(Member member, YeopjeonAction action, Integer rewardYeopjeon, Boolean isRollback) {
    Yeopjeon yeopjeon = findMemberYeopjeon(member);
    int calculatedYeopjeon;

    if (!isRollback) { // 요청 시
      if (action.equals(REWARD_YEOPJEON)) { // 엽전 현상금 존재하는 경우
        calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action, rewardYeopjeon);
      } else { // 엽전 현상금 요청이 아닌경우
        calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action);
      }
    } else { // 롤백 시
      if (action.equals(REWARD_YEOPJEON)) { // 엽전 현상금 존재하는 경우
        calculatedYeopjeon = yeopjeon.getYeopjeon() - yeopjeonCalculator.calculateYeopjeon(action, rewardYeopjeon);
      } else { // 엽전 현상금 요청이 아닌경우
        calculatedYeopjeon = yeopjeon.getYeopjeon() - yeopjeonCalculator.calculateYeopjeon(action);
      }
    }

    if (calculatedYeopjeon < 0) {
      log.error("엽전 부족: 회원 {}의 현재 엽전: {}", member.getStudentId(), yeopjeon.getYeopjeon());
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    }

    return calculatedYeopjeon;
  }

  // 엽전 변동 적용
  private void applyYeopjeonChange(Member member, int newYeopjeon) {
    Yeopjeon yeopjeon = findMemberYeopjeon(member);
    yeopjeon.setYeopjeon(newYeopjeon);
    yeopjeonRepository.save(yeopjeon);
    log.info("회원 {}의 엽전 변경 완료: 새로운 엽전 개수 = {}", member.getStudentId(), newYeopjeon);
  }

  /**
   * <h3>질문 글 등록 시 필요한 엽전 개수 검증</h3>
   *
   * @param member
   * @param rewardYeopjeon
   */
  public void validateYeopjeonForQuestionPost(Member member, Integer rewardYeopjeon) {
    // 엽전 현상금이 null인 경우 0으로 설정
    if (rewardYeopjeon == null) {
      rewardYeopjeon = 0;
    } else if (rewardYeopjeon < 0) { // 음수 값인 경우 오류
      throw new CustomException(ErrorCode.QUESTION_INVALID_REWARD_YEOPJEON);
    }

    // 질문 글 등록 시 소모되는 엽전
    int createQuestionPostYeopjeon = yeopjeonCalculator.calculateYeopjeon(CREATE_QUESTION_POST);

    // 총 필요한 엽전: 질문 글 등록 시 + 엽전 현상금
    int totalRequiredYeopjeon = createQuestionPostYeopjeon + rewardYeopjeon;

    // 글 등록 가능 여부 확인
    Yeopjeon yeopjeon = findMemberYeopjeon(member);
    if (yeopjeon.getYeopjeon() <= totalRequiredYeopjeon) {
      log.error("회원: {} 의 엽전이 부족합니다.", member.getStudentId());
      log.error("현재 보유 엽전량: {}, 질문글 등록시 필요 엽전량: {}, 엽전 현상금 설정량: {}",
          yeopjeon.getYeopjeon(), createQuestionPostYeopjeon, rewardYeopjeon);
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
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
