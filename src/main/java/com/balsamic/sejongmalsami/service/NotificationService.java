package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.NotificationCommand;
import com.balsamic.sejongmalsami.object.NotificationDto;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidConfig.Priority;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  /**
   * 단일 토큰(기기) 대상 푸시 알림 전송
   *
   * @param command targetToken, title, body
   */
  @Transactional
  public NotificationDto sendNotification(NotificationCommand command) {
    try {
      // Notification 객체 생성
      Notification notification = Notification.builder()
          .setTitle(command.getTitle())
          .setBody(command.getBody())
          .build();

      // 안드로이드 설정
      AndroidConfig androidConfig = AndroidConfig.builder()
          .setPriority(Priority.HIGH)
          .build();

      // ios 설정
      ApnsConfig apnsConfig = ApnsConfig.builder()
          .setAps(
              Aps.builder()
                  .setSound("default")
                  .build()
          )
          .build();

      // 전송할 Message 생성
      Message message = Message.builder()
          .setToken(command.getToken())
          .setNotification(notification) // putData()를 통해 데이터 메시지로도 보낼 수 있음
          .setAndroidConfig(androidConfig)
          .setApnsConfig(apnsConfig)
          .build();

      // 메시지 전송
      String response = FirebaseMessaging.getInstance().send(message);
      log.debug("단일 메시지 발송 성공: {}", response);

      return NotificationDto.builder()
          .notification(notification)
          .build();
    } catch (FirebaseMessagingException e) {
      log.error("FCM 단일 메시지 발송 실패: {}", e.getMessage());
      throw new CustomException(ErrorCode.MESSAGE_SENT_FAILED);
    }
  }
}
