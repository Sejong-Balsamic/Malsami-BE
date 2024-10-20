package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.TestCommand;
import com.balsamic.sejongmalsami.object.TestDto;
import com.balsamic.sejongmalsami.service.TestService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
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
@RequestMapping("/api/test")
public class TestController {

  private final TestService testService;;

  /**
   * 자료 Multipart 파일을 업로드
   * 워드, 엑셀, PPT, PDF 파일
   */
  @PostMapping(value = "/thumbnail/save-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<TestDto> saveDocumentThumbnail(@ModelAttribute TestCommand command) {
    return ResponseEntity.ok(testService.saveDocumentThumbnail(command));
  }

  @PostMapping(value = "/thumbnail/save-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<TestDto> saveImagesThumbnail(@ModelAttribute TestCommand command) {
    return ResponseEntity.ok(testService.saveImagesThumbnail(command));
  }

}
