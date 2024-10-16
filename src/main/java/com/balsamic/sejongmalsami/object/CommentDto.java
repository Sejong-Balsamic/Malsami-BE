package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class CommentDto {

  private Comment comment;
}
