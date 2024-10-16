package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class DocumentPostDto {

  private DocumentPost documentPost;

}
