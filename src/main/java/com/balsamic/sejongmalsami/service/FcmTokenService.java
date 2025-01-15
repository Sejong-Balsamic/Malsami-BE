package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.FcmTokenCommand;
import com.balsamic.sejongmalsami.object.FcmTokenDto;
import com.balsamic.sejongmalsami.object.mongo.FcmToken;
import com.balsamic.sejongmalsami.repository.mongo.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmTokenService {

  private final FcmTokenRepository fcmTokenRepository;

  /**
   * 사용자 토큰 저장
   * 로그인 -> 새로운 토큰 저장
   *
   * @param command member, token
   * @return
   */
  @Transactional
  public FcmTokenDto saveToken(FcmTokenCommand command) {

    FcmToken fcmToken = FcmToken.builder()
        .memberId(command.getMember().getMemberId())
        .token(command.getToken())
        .build();

    return FcmTokenDto.builder()
        .fcmToken(fcmTokenRepository.save(fcmToken))
        .build();
  }

  /**
   * 사용자 토큰 삭제
   * 로그아웃 -> 기존 토큰 삭제
   *
   * @param command member, token
   */
  @Transactional
  public void deleteToken(FcmTokenCommand command) {

    fcmTokenRepository.deleteByMemberIdAndToken(
        command.getMember().getMemberId(),
        command.getToken()
    );
    log.debug("사용자: {} FCM 토큰 삭제", command.getMember().getStudentId());
  }
}
