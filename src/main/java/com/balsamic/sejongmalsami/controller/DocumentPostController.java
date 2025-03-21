package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.service.DocumentPostService;
import com.balsamic.sejongmalsami.service.LikeService;
import com.balsamic.sejongmalsami.service.PopularPostService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
  private final LikeService likeService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> saveDocumentPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(documentPostService.saveDocumentPost(command));
  }

  @Override
  @PostMapping(value = "/popular/daily")
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getDailyPopularDocumentPost() {
    return ResponseEntity.ok(popularPostService.getDailyPopularDocumentPosts());
  }

  @Override
  @PostMapping(value = "/popular/weekly")
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getWeeklyPopularDocumentPost() {
    return ResponseEntity.ok(popularPostService.getWeeklyPopularDocumentPosts());
  }

  @Override
  @PostMapping(value = "/get", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getDocumentPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(documentPostService.getDocumentPost(command));
  }

  @Override
  @PostMapping(value = "/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> filteredDocumentPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(documentPostService.filteredDocumentPost(command));
  }

  @Override
  @PostMapping(value = "/file/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<byte[]> downloadDocumentFile(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {

    command.setMember(customUserDetails.getMember());
    DocumentDto dto = documentPostService.downloadDocumentFile(command);

    // Content-Disposition 설정 (파일 이름 안전하게 인코딩)
    ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
        .filename(dto.getFileName(), StandardCharsets.UTF_8)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .contentType(MediaType.parseMediaType(dto.getMimeType()))
        .body(dto.getFileBytes());
  }

  @Override
  @PostMapping(value = "/hot-download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> getHotDownload(
      @ModelAttribute DocumentCommand command) {
    return ResponseEntity.ok(documentPostService.getHotDownload(command));
  }

  @Override
  @PostMapping(value = "/like", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<DocumentDto> documentBoardLike(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute DocumentCommand command) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(likeService.documentBoardLike(command));
  }
}
