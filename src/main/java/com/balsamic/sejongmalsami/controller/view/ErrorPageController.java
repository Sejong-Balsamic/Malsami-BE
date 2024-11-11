package com.balsamic.sejongmalsami.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
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
