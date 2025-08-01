package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.post.object.mongo.QuestionBoardLike;
import com.balsamic.sejongmalsami.post.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class QuestionDto {

  private QuestionPost questionPost; // 질문

  private Page<QuestionPost> questionPostsPage; // 질문

  private AnswerPost answerPost; // 답변

  private List<AnswerPost> answerPosts; // 답변

  // 첨부파일
  private List<MediaFile> mediaFiles; // 질문, 답변

  // 커스텀 태그
  private List<String> customTags; // 질문

  // 좋아요 내역
  private QuestionBoardLike questionBoardLike;
}
