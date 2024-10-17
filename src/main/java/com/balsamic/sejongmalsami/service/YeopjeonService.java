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

  // 엽전 보상 처리
  @Transactional
  public void updateYeopjeon(Member member, YeopjeonAction action) {
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

    // 엽전 업데이트
    int updateYeopjeon = yeopjeon.getResultYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action);
    if (updateYeopjeon <= 0) {
      yeopjeon.changeResultYeopjeon(0);
    } else {
      yeopjeon.changeResultYeopjeon(updateYeopjeon);
    }
    yeopjeonRepository.save(yeopjeon);
  }

  // 사용자의 현재 총 엽전 수 조회
  @Transactional(readOnly = true)
  public int getResultYeopjeon(Member member) {
    return yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_YEOPJEON_NOT_FOUND))
        .getResultYeopjeon();
  }
}
