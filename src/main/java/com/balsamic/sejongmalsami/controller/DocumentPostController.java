package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.DocumentPostCommand;
import com.balsamic.sejongmalsami.object.DocumentPostDto;
import com.balsamic.sejongmalsami.service.DocumentPostService;
import com.balsamic.sejongmalsami.service.PopularPostService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
@RequestMapping("/api/document")
@Tag(
    name = "자료 게시판 API",
    description = "자료 게시글 관련 API 제공"
)
public class DocumentPostController implements DocumentPostControllerDocs {

  private final DocumentPostService documentPostService;
  private final PopularPostService popularPostService;


  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentPostDto> saveDocumentPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentPostCommand command) {
    return ResponseEntity.ok(documentPostService.saveDocumentPost(command));
  }

  @Override
  @PostMapping(value = "/daily/popular", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<List<DocumentPostDto>> getDailyPopularDocumentPost(
      @ModelAttribute DocumentPostCommand command) {
    return ResponseEntity.ok(popularPostService.getDailyPopularDocumentPosts());
  }

  @Override
  @PostMapping(value = "/weekly/popular", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<List<DocumentPostDto>> getWeeklyPopularDocumentPost(DocumentPostCommand command) {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularDocumentPosts());
  }

}
