package com.balsamic.sejongmalsami.web.controller;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.object.NotificationCommand;
import com.balsamic.sejongmalsami.object.NotificationDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface NotificationControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.01.15",
          author = Author.BAEKJIHOON,
          description = "특정 사용자 알림 발송"
      )
  })
  @Operation(
      summary = "특정 사용자 알림 발송",
      description = """
          **특정 알림 발송**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**
          
          - **UUID memberId**: 알림 발송 대상 PK [memberId, fcmToken 택 1]

          - **String fcmToken**: 알림 발송 대상 token [memberId, fcmToken 택 1]
          
          - **NotificationCategory notificationCategory**: 알림 카테고리 [필수]

          - **String title**: 알림 제목 [선택]

          - **String body**: 알림 내용 [선택]

          **반환 파라미터 값:**

          - **NotificationDto**: 알림 정보
            - **Notification notification**: 발송 알림 정보
          """
  )
  ResponseEntity<NotificationDto> sendNotification(
      CustomUserDetails customUserDetails,
      NotificationCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.01.21",
          author = Author.BAEKJIHOON,
          description = "전체 사용자 알림 발송"
      )
  })
  @Operation(
      summary = "전체 사용자 알림 발송",
      description = """
          **전체 알림 발송**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**
          
          - **NotificationCategory notificationCategory**: 알림 카테고리 [필수]

          - **String title**: 알림 제목 [선택]

          - **String body**: 알림 내용 [선택]

          **반환 파라미터 값:**

          `없음`
          """
  )
  ResponseEntity<Void> sendNotificationToAll(
      CustomUserDetails customUserDetails,
      NotificationCommand command);
}
