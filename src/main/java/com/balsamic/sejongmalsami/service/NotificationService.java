package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.NotificationCommand;
import com.balsamic.sejongmalsami.object.NotificationDto;
import com.balsamic.sejongmalsami.object.constants.NotificationCategory;
import com.balsamic.sejongmalsami.object.mongo.FcmToken;
import com.balsamic.sejongmalsami.repository.mongo.FcmTokenRepository;
import com.balsamic.sejongmalsami.util.CommonUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidConfig.Priority;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final FcmTokenRepository fcmTokenRepository;
  @Qualifier("applicationTaskExecutor")
  private final TaskExecutor taskExecutor;

  /**
   * 단일 토큰(기기) 대상 푸시 알림 전송
   *
   * @param command targetToken, notificationCategory, title, body, dataMap
   */
  @Transactional
  public NotificationDto sendNotificationByToken(NotificationCommand command) {
    try {
      // 알림 카테고리 가져오기
      NotificationCategory category = command.getNotificationCategory();
      if (category == null) {
        log.error("알림 카테고리가 설정되지 않았습니다.");
        throw new CustomException(ErrorCode.INVALID_NOTIFICATION_CATEGORY);
      }

      // customTitle이 있으면 사용, 없으면 카테고리 defaultTitle 사용
      String title = getTitleByCategory(command);

      // customBody가 있으면 사용, 없으면 카테고리 defaultBody 사용
      String body = getBodyByCategory(command);

      // 동적 template 치환
      title = applyTemplate(title, command.getTempleteValueMap());
      body = applyTemplate(body, command.getTempleteValueMap());

      // Notification 객체 생성
      Notification notification = Notification.builder()
          .setTitle(title)
          .setBody(body)
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
          .setToken(command.getFcmToken())
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

  /**
   * 모든 사용자에게 푸시 알림 전송
   *
   * @param command notificationCategory, title, body, dataMap
   */
  @Transactional
  public void sendNotificationToAll(NotificationCommand command) {
    // 전체 회원 FCM 토큰 조회
    List<String> allTokens = fcmTokenRepository.findAll().stream()
        .map(FcmToken::getFcmToken)
        .collect(Collectors.toList());

    if (allTokens.isEmpty()) {
      log.warn("알림 전송 대상이 없습니다.");
      return;
    }

    // FCM 멀티캐스트 (최대 500개/1회) -> 청크 분할
    List<List<String>> chunks = CommonUtil.partitionList(allTokens, 500);

    List<CompletableFuture<Void>> futures = new ArrayList<>();

    // 멀티스레드 처리
    for (List<String> chunk : chunks) {
      // 각 청크 비동기 작업
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        try {
          sendMulticast(command, chunk);
        } catch (Exception e) {
          log.error("FCM 멀티캐스트 전송 중 오류: {}", e.getMessage());
        }
      }, taskExecutor);
      futures.add(future);
    }

    CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    allFutures.whenComplete((result, throwable) -> {
      if (throwable != null) {
        // 전체 청크 중 하나라도 예외 발생 시
        log.error("전체 사용자 알림 발송 중 일부 실패: {}", throwable.getMessage());
      } else {
        // 모든 청크가 정상적으로 완료
        log.debug("전체 사용자 알림 전송 비동기 작업 완료");
      }
    });
  }

  /**
   * 실제 FCM 멀티캐스트 전송 로직
   *
   * @param command
   * @param tokens
   */
  private void sendMulticast(NotificationCommand command, List<String> tokens) throws FirebaseMessagingException {
    // 알림 카테고리 가져오기
    NotificationCategory category = command.getNotificationCategory();
    if (category == null) {
      log.error("알림 카테고리가 설정되지 않았습니다.");
      throw new CustomException(ErrorCode.INVALID_NOTIFICATION_CATEGORY);
    }

    // customTitle이 있으면 사용, 없으면 카테고리 defaultTitle 사용
    String title = getTitleByCategory(command);

    // customBody가 있으면 사용, 없으면 카테고리 defaultBody 사용
    String body = getBodyByCategory(command);

    // 동적 template 치환
    title = applyTemplate(title, command.getTempleteValueMap());
    body = applyTemplate(body, command.getTempleteValueMap());

    // Notification 객체 생성
    Notification notification = Notification.builder()
        .setTitle(title)
        .setBody(body)
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

    MulticastMessage multicastMessage = MulticastMessage.builder()
        .addAllTokens(tokens)
        .setNotification(notification)
        .setAndroidConfig(androidConfig)
        .setApnsConfig(apnsConfig)
        .build();

    // 알림 전송
    BatchResponse batchResponse = FirebaseMessaging.getInstance().sendEachForMulticast(multicastMessage);
    int successCount = batchResponse.getSuccessCount();
    int failureCount = batchResponse.getFailureCount();
    log.debug("FCM 멀티캐스트 전송 완료: 성공 {}건, 실패 {}건", successCount, failureCount);
  }

  /**
   * 제목을 설정하지 않은경우 카테고리에 해당하는 defaultTitle 반환 제목을 설정한 경우 커스텀 제목 반환
   */
  private String getTitleByCategory(NotificationCommand command) {
    // 커스텀 제목 존재
    if (command.getTitle() != null && !command.getTitle().isBlank()) {
      return command.getTitle();
    }
    return command.getNotificationCategory().getDefaultTitle();
  }

  /**
   * body를 설정하지 않은경우 카테고리에 해당하는 defaultBody 반환 body를 설정한 경우 커스텀 body 반환
   */
  private String getBodyByCategory(NotificationCommand command) {
    // 커스텀 body 존재
    if (command.getBody() != null && !command.getBody().isBlank()) {
      return command.getBody();
    }
    return command.getNotificationCategory().getDefaultBody();
  }

  /**
   * placeholder 치환 메서드
   */
  private String applyTemplate(String template, Map<String, String> dataMap) {
    if (template == null) {
      return "";
    }
    if (dataMap == null || dataMap.isEmpty()) {
      return template;
    }

    String result = template;
    for (Map.Entry<String, String> entry : dataMap.entrySet()) {
      String placeholder = "{" + entry.getKey() + "}";
      result = result.replace(placeholder, entry.getValue());
    }

    return result;
  }
}
