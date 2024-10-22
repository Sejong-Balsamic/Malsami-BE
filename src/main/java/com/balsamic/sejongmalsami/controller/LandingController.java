package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.service.PopularPostService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/landing")
@Tag(
    name = "랜딩 페이지 API",
    description = "랜딩 페이지 관련 API 제공"
)
public class LandingController implements LandingControllerDocs {

  private final PopularPostService popularPostService;

  @Override
  @PostMapping(value = "/popular/question/daily", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getDailyPopularQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(popularPostService.getDailyPopularQuestionPosts());
  }

  @Override
  @PostMapping(value = "/popular/question/weekly", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularQuestionPosts());
  }

  @Override
  @PostMapping(value = "/popular/document/daily", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getDailyPopularDocumentPost(
      @ModelAttribute DocumentCommand command) {
    return ResponseEntity.ok(popularPostService.getDailyPopularDocumentPosts());
  }

  @Override
  @PostMapping(value = "/popular/document/weekly", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getWeeklyPopularDocumentPost(DocumentCommand command) {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularDocumentPosts());
  }
}
