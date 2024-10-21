package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.YeopjeonDto;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.YeopjeonCalculator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class YeopjeonService {

  private final YeopjeonRepository yeopjeonRepository;
  private final YeopjeonCalculator yeopjeonCalculator;

  // 엽전 보상 처리 (비동기)
  @Async
  public YeopjeonDto updateYeopjeon(Member member, YeopjeonAction action) {
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));

    // 엽전 업데이트
    int updateYeopjeon = yeopjeon.getResultYeopjeon() + yeopjeonCalculator.calculateYeopjeon(action);
    if (updateYeopjeon <= 0) {
      yeopjeon.updateResultYeopjeon(0);
    } else {
      yeopjeon.updateResultYeopjeon(updateYeopjeon);
    }
    log.info("엽전 개수 업데이트 완료: {}의 엽전 개수 = {}", member.getStudentId(), yeopjeon.getResultYeopjeon());

    return YeopjeonDto.builder()
        .yeopjeon(yeopjeonRepository.save(yeopjeon))
        .build();
  }

  // 사용자의 현재 총 엽전 수 조회
  @Transactional(readOnly = true)
  public YeopjeonDto getResultYeopjeon(Member member) {
    return YeopjeonDto.builder()
        .yeopjeon(yeopjeonRepository.findByMember(member)
            .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND)))
        .build();
  }
}
