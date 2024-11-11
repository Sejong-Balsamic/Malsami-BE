package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

  private final JwtUtil jwtUtil;

  @GetMapping("/")
  public String IndexPage(Model model) {
    return "index";
  }

  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/admin/question")
  public String questionPage() {
    return "admin/question";
  }

  @GetMapping("/admin/document")
  public String documentPage() {
    return "admin/document";
  }

  @GetMapping("/admin/dashboard")
  public String dashboardPage(
      @RequestParam(required = false) String accessToken) {
    // 토큰 검증
    if (accessToken != null && jwtUtil.validateToken(accessToken)) {
      return "admin/dashboard";
    }
    return "redirect:/login";
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
