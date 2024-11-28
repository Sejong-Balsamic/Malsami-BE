package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.CommentLike;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class CommentDto {

  private Comment comment; // 특정 댓글

  private Page<Comment> commentsPage; // 댓글 List

  private CommentLike commentLike; // 댓글 좋아요 내역
}
