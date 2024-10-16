package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QuestionPostDto {

  private QuestionPost questionPost;

  // 첨부파일
  private List<MediaFileDto> mediaFiles;

  // 커스텀 태그
  private Set<String> customTags;
}
