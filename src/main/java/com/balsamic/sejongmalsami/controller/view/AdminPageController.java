package com.balsamic.sejongmalsami.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

  @GetMapping("/")
  public String home(Model model) {
    return "index";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/admin/question")
  public String question() {
    return "admin/question";
  }

  @GetMapping("/admin/document")
  public String document() {
    return "admin/document";
  }
}
