package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.FcmTokenCommand;
import com.balsamic.sejongmalsami.object.FcmTokenDto;
import com.balsamic.sejongmalsami.object.mongo.FcmToken;
import com.balsamic.sejongmalsami.repository.mongo.FcmTokenRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
   * 기존 토큰이 존재하는 경우 -> 저장된 토큰 반환
   *
   * @param command member, fcmToken
   * @return
   */
  @Transactional
  public FcmTokenDto saveToken(FcmTokenCommand command) {

    if (command.getFcmToken() == null || command.getFcmToken().isBlank()) {
      log.error("FCM 토큰이 존재하지 않습니다.");
      throw new CustomException(ErrorCode.INVALID_FCM_TOKEN);
    }

    Boolean isExists = fcmTokenRepository.existsByFcmToken(command.getFcmToken());
    if (isExists) {
      log.debug("사용자의 FCM 토큰이 이미 존재합니다.");
      return FcmTokenDto.builder()
          .fcmToken(fcmTokenRepository.findByFcmToken(command.getFcmToken()))
          .build();
    }

    FcmToken fcmToken = FcmToken.builder()
        .memberId(command.getMember().getMemberId())
        .fcmToken(command.getFcmToken())
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

    fcmTokenRepository.deleteByMemberIdAndFcmToken(
        command.getMember().getMemberId(),
        command.getFcmToken()
    );
    log.debug("사용자: {} FCM 토큰 삭제", command.getMember().getStudentId());
  }
}
