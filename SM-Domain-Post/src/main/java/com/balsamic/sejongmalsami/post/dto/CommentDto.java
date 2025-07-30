package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.post.object.mongo.CommentLike;
import com.balsamic.sejongmalsami.post.object.postgres.Comment;
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
