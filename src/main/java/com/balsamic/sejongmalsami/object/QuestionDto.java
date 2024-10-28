package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.QuestionBoardLike;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
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

  private List<QuestionPost> questionPosts; // 질문

  private AnswerPost answerPost; // 답변

  private List<AnswerPost> answerPosts; // 답변

  // 첨부파일
  private List<MediaFile> mediaFiles; // 질문, 답변

  // 커스텀 태그
  private Set<String> customTags; // 질문

  // 좋아요 내역
  private QuestionBoardLike questionBoardLike;
}
