package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.NotificationCategory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NotificationCommand {

  private Member member;
  private UUID memberId;

  private String fcmToken;
  private NotificationCategory notificationCategory;
  private String title;
  private String body;

  // NotificationCategory 템플릿에 치환할 값
  private Map<String, String> templeteValueMap;
}
