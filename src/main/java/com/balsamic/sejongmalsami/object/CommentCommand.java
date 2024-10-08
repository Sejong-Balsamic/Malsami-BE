package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class CommentCommand {

  private UUID memberId;
  private String content;
  private UUID postId;
  private ContentType contentType;
  private Boolean isPrivate;
}
