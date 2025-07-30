package com.balsamic.sejongmalsami.application.dto;

import com.google.firebase.messaging.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class NotificationDto {

  private Notification notification;
}
