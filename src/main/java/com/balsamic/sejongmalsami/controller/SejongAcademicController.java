package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.SejongAcademicCommand;
import com.balsamic.sejongmalsami.object.SejongAcademicDto;
import com.balsamic.sejongmalsami.service.SejongAcademicService;
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
@RequestMapping("/api/sejong")
@Tag(
    name = "세종대학교 학문 데이터 API",
    description = "세중대학교 학문적 데이터 정보 제공"
)
public class SejongAcademicController implements SejongAcademicControllerDocs {

  private final SejongAcademicService sejongAcademicService;

  @PostMapping(value = "/faculty/get-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<SejongAcademicDto> getAllFaculties(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute SejongAcademicCommand command) {
    return ResponseEntity.ok(sejongAcademicService.getAllFaculties(command));
  }
}
