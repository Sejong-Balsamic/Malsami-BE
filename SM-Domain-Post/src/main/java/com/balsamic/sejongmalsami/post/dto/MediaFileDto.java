package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.postgres.MediaFile;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class MediaFileDto {

  private MediaFile mediaFile;
}
