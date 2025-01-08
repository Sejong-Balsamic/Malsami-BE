package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.ReportCommand;
import com.balsamic.sejongmalsami.object.ReportDto;
import com.balsamic.sejongmalsami.service.ReportService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/report")
@Tag(
    name = "신고 API",
    description = "신고 관련 API 제공"
)
public class ReportController implements ReportControllerDocs {

  private final ReportService reportService;

  @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<ReportDto> saveReportPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute ReportCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(reportService.saveReportPost(command));
  }
}
