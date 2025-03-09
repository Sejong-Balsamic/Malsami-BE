package com.balsamic.sejongmalsami.util.deprecated;

import com.balsamic.sejongmalsami.object.postgres.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Deprecated
@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class FcmTokenCommand {

  private Member member;

  private String fcmToken; // 푸시 알림을 전송할 FCM 토큰
}
