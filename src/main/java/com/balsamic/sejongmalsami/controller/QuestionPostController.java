package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.service.PopularPostService;
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
@RequestMapping("/api/questions")
@Tag(
    name = "질문 게시글 API",
    description = "질문 게시글 관련 API 제공"
)
public class QuestionPostController implements QuestionPostControllerDocs {

  private final QuestionPostService questionPostService;
  private final PopularPostService popularPostService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> saveQuestionPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(questionPostService.saveQuestionPost(command));
  }

  @Override
  @PostMapping(value = "/get", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<QuestionDto> getQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(questionPostService.findQuestionPost(command));
  }

  @Override
  @PostMapping(value = "/get/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getAllQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(questionPostService.findAllQuestionPost());
  }

  @Override
  @PostMapping(value = "/popular/daily", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getDailyPopularQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(popularPostService.getDailyPopularQuestionPosts());
  }

  @Override
  @PostMapping(value = "/popular/weekly", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularQuestionPosts());
  }
}
