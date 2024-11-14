package com.balsamic.sejongmalsami.controller.view;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(
    name = "관리자 WEB 에러 페이지 API",
    description = "관리자 WEB 에러 페이지 API 제공"
)
public class ErrorPageController {

  @GetMapping("/error/403")
  public String forbidden() {
    return "error/403";
  }

  @GetMapping("/error/404")
  public String notFound() {
    return "error/404";
  }

  @GetMapping("/error/500")
  public String serverError() {
    return "error/500";
  }
}
