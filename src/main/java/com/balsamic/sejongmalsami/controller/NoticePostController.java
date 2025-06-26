package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.NoticePostCommand;
import com.balsamic.sejongmalsami.object.NoticePostDto;
import com.balsamic.sejongmalsami.service.NoticePostService;
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
@RequestMapping("/api/notice")
@Tag(
    name = "공지사항 게시글 API",
    description = "공지사항 게시글 관련 API 제공"
)
public class NoticePostController implements NoticePostControllerDocs {

  private final NoticePostService noticePostService;

  @Override
  @PostMapping(value = "/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<NoticePostDto> getFilteredNoticePosts(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute NoticePostCommand command) {
    return ResponseEntity.ok(noticePostService.getFilteredPost(command));
  }

  @Override
  @PostMapping(value = "/get/pinned", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<NoticePostDto> getPinnedNoticePost() {
    return ResponseEntity.ok(noticePostService.getPinnedPost());
  }
}
