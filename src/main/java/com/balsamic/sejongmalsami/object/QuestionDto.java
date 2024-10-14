package com.balsamic.sejongmalsami.object;

import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QuestionDto {

  private QuestionPost questionPost; // 질문

  private AnswerPost answerPost; // 답변

  // 첨부파일
  private List<MediaFileDto> mediaFiles; // 질문, 답변

  // 커스텀 태그
  private Set<String> customTags; // 질문
}
