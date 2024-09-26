package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionPostCommand;
import com.balsamic.sejongmalsami.object.QuestionPostDto;
import com.balsamic.sejongmalsami.service.QuestionPostService;
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
@RequestMapping("/api/question")
@Tag(
    name = "질문 게시판 API",
    description = "질문 게시글 관련 API 제공"
)
public class QuestionPostController implements QuestionPostControllerDocs {

  private final QuestionPostService questionPostService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionPostDto> saveQuestionPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionPostCommand command) {
    command.setMemberId(customUserDetails.getUsername());
    return ResponseEntity.ok(questionPostService.saveQuestionPost(command));
  }
}
