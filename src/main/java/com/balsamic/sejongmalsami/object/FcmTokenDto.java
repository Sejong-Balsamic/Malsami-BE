package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.FcmToken;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FcmTokenDto {

  private FcmToken fcmToken;
}