package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.WebLoginDto;
import com.balsamic.sejongmalsami.service.AuthService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Tag(
    name = "관리자 WEB  페이지 API",
    description = "관리자 WEB 에러 페이지 API 제공"
)
public class AdminAuthController implements  AdminAuthControllerDocs{

  private final AuthService authService;

  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<WebLoginDto> webLogin(
      @ModelAttribute  MemberCommand command, HttpServletResponse response) {
    return ResponseEntity.ok(authService.webLogin(command, response));
  }
}