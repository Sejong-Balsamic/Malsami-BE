package com.balsamic.sejongmalsami.object;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class AnswerPostDto {

  private AnswerPost answerPost;

  // 첨부파일
  private List<MediaFileDto> mediaFiles;
  
}