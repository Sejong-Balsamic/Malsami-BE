package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.AdminCommand;
import com.balsamic.sejongmalsami.object.AdminDto;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.LogUtils;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminApiService {

  private static final Logger log = LoggerFactory.getLogger(AdminApiService.class);
  private final MemberRepository memberRepository;
  private final YeopjeonRepository yeopjeonRepository;

  public AdminDto processUuidPacchingko(AdminCommand command) {
    // member 가져오기
    Member member = command.getMember();

    // 엽전 가져오기
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));

    // 엽전 -1 가능한지 확인 후 -1
    if(yeopjeon.getYeopjeon() < 1){
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    } else {
      yeopjeon.setYeopjeon(yeopjeon.getYeopjeon() - 1);
      yeopjeonRepository.save(yeopjeon);
    }

    // member uuid 변경 후 저장
    String newUuidNickName = UUID.randomUUID().toString().substring(0, 6);
    member.setUuidNickname(newUuidNickName);

    // 로깅
    LogUtils.lineLog("새로운UUID : " + member.getStudentId() + " : " + newUuidNickName);

    return AdminDto.builder()
        .member(member)
        .yeopjeon(yeopjeon)
        .build();
  }
}
