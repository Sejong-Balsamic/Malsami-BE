package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.application.dto.NotificationCommand;
import com.balsamic.sejongmalsami.application.dto.NotificationDto;
import com.balsamic.sejongmalsami.application.service.NotificationService;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(
    name = "FCM 알림 API",
    description = "알림 발송 관련 API 제공"
)
public class NotificationController implements NotificationControllerDocs {

  private final NotificationService notificationService;

  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<NotificationDto> sendNotification(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute NotificationCommand command) {
    return ResponseEntity.ok(notificationService.sendNotificationByToken(command));
  }

  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/send/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Void> sendNotificationToAll(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute NotificationCommand command) {
    notificationService.sendNotificationToAll(command);
    return ResponseEntity.ok().build();
  }

}
