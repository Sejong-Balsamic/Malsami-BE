package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.AnswerPostCommand;
import com.balsamic.sejongmalsami.object.AnswerPostDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.service.AnswerPostService;
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
@RequestMapping("/api/answers")
@Tag(
    name = "질문게시판 답변글 API",
    description = "답변글 관련 API 제공"
)
public class AnswerPostController implements AnswerPostControllerDocs {

  private final AnswerPostService answerPostService;

  @Override
  @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AnswerPostDto> saveAnswerPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AnswerPostCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(answerPostService.saveAnswer(command));
  }
}
