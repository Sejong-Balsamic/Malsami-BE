package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.constants.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CommentCommand {
  public CommentCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

  private UUID memberId;
  private UUID postId;
  private String content;
  private ContentType contentType;
  private Boolean isPrivate;
  @Schema(defaultValue = "0")
  private Integer pageNumber = 0; // n번째 페이지 조회
  @Schema(defaultValue = "30")
  private Integer pageSize = 30; // n개의 데이터 조회
}
