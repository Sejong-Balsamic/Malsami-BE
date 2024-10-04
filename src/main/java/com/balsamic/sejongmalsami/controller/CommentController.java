package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.CommentService;
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
public class CommentController implements CommentControllerDocs{

  private final CommentService commentService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<CommentDto> saveComment(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute CommentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(commentService.addComment(command));
  }
}
