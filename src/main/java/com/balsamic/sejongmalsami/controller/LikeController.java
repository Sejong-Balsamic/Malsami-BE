package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.service.LikeService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/likes")
@Tag(
    name = "좋아요 API",
    description = "게시물 좋아요 관련 API 제공"
)
public class LikeController implements LikeControllerDocs{

  private final LikeService likeService;

  @Override
  @PostMapping(value = "/question/board", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> questionBoardLike(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(likeService.questionBoardLike(command));
  }

  @Override
  @PostMapping(value = "/comment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<CommentDto> commentLike(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute CommentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(likeService.commentLike(command));
  }

}
