package com.balsamic.sejongmalsami.object;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QuestionPostDto {

  private QuestionPost questionPost;

  // 커스텀 태그
  private List<String> customTags;
}
