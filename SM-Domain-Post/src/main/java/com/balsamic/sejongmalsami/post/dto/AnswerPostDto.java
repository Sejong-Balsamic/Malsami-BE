package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.object.MediaFileDto;
import com.balsamic.sejongmalsami.postgres.AnswerPost;
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
