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

    if (updateYeopjeon < 0) {
      log.error("엽전이 부족합니다: {}의 엽전 개수 = {}", member.getStudentId(), yeopjeon.getResultYeopjeon());
      yeopjeon.updateResultYeopjeon(0);  // 엽전 개수를 0으로 설정
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    } else {
      yeopjeon.updateResultYeopjeon(updateYeopjeon);
    }

    log.info("엽전 개수 업데이트 완료: {}의 엽전 개수 = {}", member.getStudentId(), yeopjeon.getResultYeopjeon());

    yeopjeonRepository.save(yeopjeon);
  }

  // 사용자의 현재 총 엽전 수 조회
  @Transactional(readOnly = true)
  public YeopjeonDto getResultYeopjeon(Member member) {
    return YeopjeonDto.builder()
        .yeopjeon(yeopjeonRepository.findByMember(member)
            .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND)))
        .build();
  }

  // 엽전 개수 롤백
  @Transactional
  public void rollbackYeopjeon(Member member, YeopjeonAction action) {

    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));

    log.info("엽전 수 롤백 전 - 회원: {}, 롤백 전 엽전 수: {}", member.getStudentId(), yeopjeon.getResultYeopjeon());
    yeopjeon.updateResultYeopjeon(yeopjeon.getResultYeopjeon()
        - yeopjeonCalculator.calculateYeopjeon(action));
    log.info("엽전 수 롤백 후 - 회원: {}, 롤백 후 엽전 수: {}", member.getStudentId(), yeopjeon.getResultYeopjeon());
  }
}
