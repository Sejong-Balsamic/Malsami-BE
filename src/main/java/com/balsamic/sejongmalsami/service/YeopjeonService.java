package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
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

  // 엽전 변동 로직 및 엽전 히스토리 내역 저장 (롤백 포함)
  @Transactional
  public YeopjeonHistory updateYeopjeonAndSaveYeopjeonHistory(Member member, YeopjeonAction action) {
    YeopjeonHistory yeopjeonHistory;

    updateMemberYeopjeon(member, action);

    try {
      yeopjeonHistory = yeopjeonHistoryService.saveYeopjeonHistory(member, action);
      log.info("엽전 히스토리 저장 성공");
    } catch (Exception e) {
      log.error("엽전 히스토리 저장 시 오류가 발생했습니다. 오류내용: {}", e.getMessage());
      log.info("엽전 롤백을 진행합니다.");
      rollbackYeopjeon(member, action);
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
      log.info("엽전 롤백 및 엽전 내역 삭제를 진행합니다.");
      yeopjeonHistoryService.deleteYeopjeonHistory(yeopjeonHistory);
      rollbackYeopjeon(member, action);
    } catch (Exception e) {
      log.error("엽전 롤백 과정에서 오류가 발생했습니다.");
      throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  // 엽전 증감 로직
  private void updateMemberYeopjeon(Member member, YeopjeonAction action) {

    Yeopjeon yeopjeon = findMemberYeopjeon(member);

    // 엽전 개수 변동
    int calculatedYeopjeon = yeopjeon.getYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action);

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
  private void rollbackYeopjeon(Member member, YeopjeonAction action) {

    Yeopjeon yeopjeon = findMemberYeopjeon(member);

    log.info("엽전 수 롤백 전 - 회원: {}, 엽전 수: {}", member.getStudentId(), yeopjeon.getYeopjeon());
    yeopjeon.updateYeopjeon(yeopjeon.getYeopjeon()
                            - yeopjeonCalculator.calculateYeopjeon(action));
    log.info("엽전 수 롤백 후 - 회원: {}, 롤백 후 엽전 수: {}", member.getStudentId(), yeopjeon.getYeopjeon());
  }

  // 사용자의 엽전 테이블 반환 메서드
  public Yeopjeon findMemberYeopjeon(Member member) {
    return yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));
  }
}
