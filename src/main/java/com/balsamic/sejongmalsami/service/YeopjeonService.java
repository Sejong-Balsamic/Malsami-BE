package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
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
  public void updateYeopjeonAndSaveYeopjeonHistory(Member member, YeopjeonAction action) {
    updateMemberYeopjeon(member, action);

    try {
      yeopjeonHistoryService.saveYeopjeonHistory(member, action);
      log.info("엽전 히스토리 저장 성공");
    } catch (Exception e) {
      log.error("엽전 히스토리 저장 시 오류가 발생했습니다. 오류내용: {}", e.getMessage());
      log.info("엽전 롤백을 진행합니다.");
      rollbackYeopjeon(member, action);
      throw new CustomException(ErrorCode.YEOPJEON_SAVE_ERROR);
    }
  }

  // 엽전 증감 로직
  @Transactional
  public void updateMemberYeopjeon(Member member, YeopjeonAction action) {

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
  @Transactional
  public void rollbackYeopjeon(Member member, YeopjeonAction action) {

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
