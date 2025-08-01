package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.post.dto.CommentCommand;
import com.balsamic.sejongmalsami.post.dto.CommentDto;
import com.balsamic.sejongmalsami.post.service.CommentService;
import com.balsamic.sejongmalsami.post.service.LikeService;
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
@RequestMapping("/api/comment")
@Tag(
    name = "댓글 API",
    description = "댓글 관련 API 제공"
)
public class CommentController implements CommentControllerDocs {

  private final CommentService commentService;
  private final LikeService likeService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<CommentDto> saveComment(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute CommentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(commentService.addComment(command));
  }

  @Override
  @PostMapping(value = "/get/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<CommentDto> getAllCommentsByPostId(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute CommentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(commentService.getAllCommentsByPostId(command));
  }

  @Override
  @PostMapping(value = "/like", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<CommentDto> commentLike(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute CommentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(likeService.commentLike(command));
  }
}
