package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.service.DocumentRequestPostService;
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
@RequestMapping("/api/document-request")
@Tag(
    name = "자료요청 게시판 API",
    description = "자료요청 게시판 관련 API 제공"
)
public class DocumentRequestPostController implements DocumentRequestPostControllerDocs{

  private final DocumentRequestPostService documentRequestPostService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> saveDocumentRequestPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(documentRequestPostService.createPost(command));
  }

  @Override
  @PostMapping(value = "/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getFilteredDocumentRequestPosts(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(documentRequestPostService.filteredDocumentRequests(command));
  }

  @Override
  @PostMapping(value = "/get", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getDocumentRequestPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(documentRequestPostService.getDocumentRequestPost(command));
  }
}
