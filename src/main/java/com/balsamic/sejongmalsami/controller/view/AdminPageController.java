package com.balsamic.sejongmalsami.controller.view;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

  @GetMapping("/")
  public String IndexPage(Model model) {
    return "index";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/question")
  public String questionPage() {
    return "admin/question";
  }

  @GetMapping("/document")
  public String documentPage() {
    return "admin/document";
  }

  @GetMapping("/dashboard")
  public String dashboardPage() {
    return "admin/dashboard";
  }

  @GetMapping("/logout")
  public String logout(HttpServletResponse response) {
    // 리프레시 토큰 쿠키 삭제
    Cookie cookie = new Cookie("refreshToken", null);
    cookie.setMaxAge(0);
    cookie.setPath("/");
    response.addCookie(cookie);

    return "redirect:/login";
  }
}
