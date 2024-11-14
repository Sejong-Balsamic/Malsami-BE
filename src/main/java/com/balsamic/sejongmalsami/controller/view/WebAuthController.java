package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.WebLoginDto;
import com.balsamic.sejongmalsami.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/web/auth")
@RequiredArgsConstructor
@Slf4j
public class WebAuthController {

  private final AuthService authService;

  @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<WebLoginDto> webLogin(
      @ModelAttribute  MemberCommand command, HttpServletResponse response) {
    return ResponseEntity.ok(authService.webLogin(command, response));
  }
}