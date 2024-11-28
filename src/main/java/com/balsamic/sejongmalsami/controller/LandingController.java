package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.service.DocumentPostService;
import com.balsamic.sejongmalsami.service.DocumentRequestPostService;
import com.balsamic.sejongmalsami.service.PopularPostService;
import com.balsamic.sejongmalsami.service.QuestionPostService;
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

  private final QuestionPostService questionPostService;
  private final DocumentPostService documentPostService;
  private final DocumentRequestPostService documentRequestPostService;
  private final PopularPostService popularPostService;

  @Override
  @PostMapping(value = "/question", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getFilteredQuestionPosts(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(questionPostService.filteredQuestions(command));
  }

  @Override
  @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getFilteredDocumentPosts(
      @ModelAttribute DocumentCommand command) {
    return ResponseEntity.ok(documentPostService.filteredDocumentPost(command));
  }

  @Override
  @PostMapping(value = "/document-request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getFilteredDocumentRequestPosts(
      @ModelAttribute DocumentCommand command) {
    return ResponseEntity.ok(documentRequestPostService.filteredDocumentRequests(command));
  }

  @Override
  @PostMapping(value = "/popular/question/daily", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getDailyPopularQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(popularPostService.getDailyPopularQuestionPosts(command));
  }

  @Override
  @PostMapping(value = "/popular/question/weekly", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularQuestionPosts(command));
  }

  @Override
  @PostMapping(value = "/popular/document/daily", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getDailyPopularDocumentPost(
      @ModelAttribute DocumentCommand command) {
    return ResponseEntity.ok(popularPostService.getDailyPopularDocumentPosts(command));
  }

  @Override
  @PostMapping(value = "/popular/document/weekly", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getWeeklyPopularDocumentPost(
      @ModelAttribute DocumentCommand command) {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularDocumentPosts(command));
  }
}
