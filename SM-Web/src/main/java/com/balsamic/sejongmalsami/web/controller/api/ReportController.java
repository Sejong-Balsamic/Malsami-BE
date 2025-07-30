package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.post.dto.ReportCommand;
import com.balsamic.sejongmalsami.post.dto.ReportDto;
import com.balsamic.sejongmalsami.post.service.ReportService;
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
@RequestMapping("/api/report")
@Tag(
    name = "신고 API",
    description = "신고 관련 API 제공"
)
public class ReportController implements ReportControllerDocs {

  private final ReportService reportService;

  @Override
  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<ReportDto> saveReportPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute ReportCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(reportService.saveReportPost(command));
  }
}
