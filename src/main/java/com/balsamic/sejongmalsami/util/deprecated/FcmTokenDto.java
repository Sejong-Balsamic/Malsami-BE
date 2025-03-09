package com.balsamic.sejongmalsami.util.deprecated;

import com.balsamic.sejongmalsami.object.mongo.FcmToken;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Deprecated
@Builder
@Getter
@ToString
public class FcmTokenDto {

  private FcmToken fcmToken;
}
