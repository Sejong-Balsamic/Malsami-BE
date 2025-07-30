package com.balsamic.sejongmalsami.web.controller;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.post.dto.QuestionDto;
import com.balsamic.sejongmalsami.post.service.AnswerPostService;
import com.balsamic.sejongmalsami.post.service.LikeService;
import com.balsamic.sejongmalsami.post.service.PopularPostService;
import com.balsamic.sejongmalsami.post.service.QuestionPostService;
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
    name = "질문 게시글 API",
    description = "질문 게시글 관련 API 제공"
)
public class QuestionPostController implements QuestionPostControllerDocs {

  private final QuestionPostService questionPostService;
  private final PopularPostService popularPostService;
  private final LikeService likeService;
  private final AnswerPostService answerPostService;

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
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getQuestionPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(questionPostService.getQuestionPost(command));
  }

  @Override
  @PostMapping(value = "/unanswered", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getAllQuestionPostsNotAnswered(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(questionPostService.findAllQuestionPostsNotAnswered(command));
  }

  @Override
  @PostMapping(value = "/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getFilteredQuestionPosts(
      @ModelAttribute QuestionCommand command) {
    return ResponseEntity.ok(questionPostService.filteredQuestions(command));
  }

  @Override
  @PostMapping(value = "/popular/daily")
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getDailyPopularQuestionPost() {
    return ResponseEntity.ok(popularPostService.getDailyPopularQuestionPosts());
  }

  @Override
  @PostMapping(value = "/popular/weekly")
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost() {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularQuestionPosts());
  }

  @Override
  @PostMapping(value = "/like", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> questionBoardLike(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(likeService.questionBoardLike(command));
  }

  @Override
  @PostMapping(value = "/answer/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> saveAnswerPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(answerPostService.saveAnswer(command));
  }

  @Override
  @PostMapping(value = "/answer/get/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> getAnswersByQuestion(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(answerPostService.getAnswersByQuestion(command));
  }

  @Override
  @PostMapping(value = "/answer/chaetaek", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QuestionDto> chaetaekAnswerPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute QuestionCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(answerPostService.chaetaekAnswer(command));
  }
}
