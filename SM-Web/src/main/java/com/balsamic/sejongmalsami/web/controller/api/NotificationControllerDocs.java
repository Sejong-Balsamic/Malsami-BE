package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.application.dto.NotificationCommand;
import com.balsamic.sejongmalsami.application.dto.NotificationDto;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
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
      특정 사용자에게 푸시 알림을 발송합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: ADMIN
      
      **요청 파라미터**
      - memberId (선택): 알림 발송 대상 회원 ID
      - fcmToken (선택): 알림 발송 대상 FCM 토큰
        * memberId 또는 fcmToken 중 하나는 필수
      - notificationCategory (필수): 알림 카테고리
        * SYSTEM: 시스템 알림
        * BOARD: 게시판 알림
        * EVENT: 이벤트 알림
      - title (선택): 알림 제목
      - body (선택): 알림 내용
      
      **응답 데이터**
      - NotificationDto: 발송된 알림 정보
        * notification: 알림 객체
        * sentAt: 발송 시간
        * status: 발송 상태
      
      **예외 상황**
      - MEMBER_NOT_FOUND (404): 대상 회원을 찾을 수 없음
      - INVALID_FCM_TOKEN (400): 유효하지 않은 FCM 토큰
      - NOTIFICATION_SEND_FAILED (500): 알림 발송 실패
      
      **참고사항**
      - 관리자만 특정 사용자에게 알림을 발송할 수 있음
      - FCM 토큰이 만료된 경우 발송 실패 가능
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
      모든 사용자에게 푸시 알림을 일괄 발송합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: ADMIN
      
      **요청 파라미터**
      - notificationCategory (필수): 알림 카테고리
        * SYSTEM: 시스템 알림
        * BOARD: 게시판 알림
        * EVENT: 이벤트 알림
      - title (선택): 알림 제목
      - body (선택): 알림 내용
      
      **응답 데이터**
      - 없음 (204 No Content)
      
      **예외 상황**
      - NOTIFICATION_SEND_FAILED (500): 알림 발송 실패
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 관리자만 전체 사용자에게 알림을 발송할 수 있음
      - 대량 발송이므로 처리 시간이 오래 걸릴 수 있음
      - 비활성 사용자나 FCM 토큰이 만료된 사용자는 발송 제외
      """
  )
  ResponseEntity<Void> sendNotificationToAll(
      CustomUserDetails customUserDetails,
      NotificationCommand command);
}
