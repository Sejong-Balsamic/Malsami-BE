package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.LikeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBoardLike extends BaseMongoEntity {

  @Id
  private String questionBoardLikeId;

  private String questionPostId; // 질문 게시글 ID

  private String memberId;      // 좋아요를 누른 사용자 ID

  private LikeType likeType;  // POST, ANSWER, COMMENT
}
