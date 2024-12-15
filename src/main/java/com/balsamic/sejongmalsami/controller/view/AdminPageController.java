package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.util.JwtUtil;
import com.balsamic.sejongmalsami.util.LogUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
@Tag(
    name = "관리자 WEB 페이지 API",
    description = "관리자 WEB 페이지 API 제공"
)
public class AdminPageController {

  private final JwtUtil jwtUtil;

  /**
   * 인덱스 페이지 - 토큰 검증 필요 없음
   */
  @GetMapping("/")
  public String indexPage(Model model) {
    return "index";
  }

  /**
   * 로그인 페이지 - 토큰 검증 필요 없음
   */
  @GetMapping("/login")
  public String loginPage() {
    return "login";
  }

  /**
   * 질문 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/question")
  public String questionPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/question";
  }

  /**
   * 문서 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/document")
  public String documentPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/document";
  }

  /**
   * 대시보드 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/dashboard")
  public String dashboardPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/dashboard";
  }

  /**
   * 테스트 페이지 1 - 토큰 검증 필요
   */
  @GetMapping("/admin/testPage1")
  public String testPage1(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/testPage1";
  }

  /**
   * 멤버 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/member")
  public String memberPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/member";
  }

  /**
   * 관리자 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/test")
  public String adminPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/test";
  }

  /**
   * 개발자의놀이터 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/play-ground")
  public String playGroundPage(@RequestParam String accessToken, Model model) {
//    if (!jwtUtil.validateToken(accessToken)) {
//      return "redirect:/error/403";
//    }
    log.info(accessToken);
    LogUtils.superLog(accessToken);
    return "admin/playGround";
  }
  /**
   * 로그아웃 - 토큰 검증 필요 없음
   */
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
