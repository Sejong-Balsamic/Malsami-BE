package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class MediaFileDto {

  private MediaFile mediaFile;
}
