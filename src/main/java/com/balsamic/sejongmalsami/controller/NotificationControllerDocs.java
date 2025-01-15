package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.NotificationCommand;
import com.balsamic.sejongmalsami.object.NotificationDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface NotificationControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.1.15",
          author = Author.BAEKJIHOON,
          description = "FCM 알림 기능 init"
      )
  })
  @Operation(
      summary = "FCM 알림 기능",
      description = """
          **알림 발송**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **String token**: 알림 발송 대상 token [필수]

          - **String title**: 알림 제목 [필수]

          - **String body**: 알림 내용 [필수]

          **반환 파라미터 값:**

          - **NotificationDto**: 알림 정보
            - **Notification notification**: 발송 알림 정보
          """
  )
  ResponseEntity<NotificationDto> sendNotification(
      CustomUserDetails customUserDetails,
      NotificationCommand command);
}
