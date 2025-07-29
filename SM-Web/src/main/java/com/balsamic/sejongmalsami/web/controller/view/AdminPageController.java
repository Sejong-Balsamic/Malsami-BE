package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.application.JwtUtil;
import com.balsamic.sejongmalsami.application.dto.AdminDto;
import com.balsamic.sejongmalsami.application.service.AdminApiService;
import com.balsamic.sejongmalsami.object.constants.NotificationCategory;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
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
  private final AdminApiService adminApiService;

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
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/playGround";
  }

  /**
   * 개발자의놀이터 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/yeopjeon")
  public String yeopjeonPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/yeopjeon";
  }

  /**
   * 서버 에러코드 페이지 - 토큰 검증 필요
   */
  @GetMapping("/admin/error-code")
  public String errorCodePage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    return "admin/errorCode";
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

  /**
   * 교과목명 - 조회
   */
  @GetMapping("/admin/subject")
  public String subjectPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    log.info(accessToken);

    // DB에서 단과대 목록 조회
    List<Faculty> faculties = adminApiService.getAllFaculties().getFaculties();
    model.addAttribute("faculties", faculties);

    // DB에서 연도 및 학기 조회
    AdminDto adminDto = adminApiService.getSubjectYearAndSemester();
    model.addAttribute("years", adminDto.getYears());
    model.addAttribute("semesters", adminDto.getSemesters());

    return "admin/subject";
  }

  /**
   * 공지사항 페이지
   */
  @GetMapping("/admin/notice")
  public String noticePage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    log.info(accessToken);

    return "admin/notice";
  }

  /**
   * 알림 페이지
   */
  @GetMapping("/admin/notification")
  public String notificationPage(@RequestParam String accessToken, Model model) {
    if (!jwtUtil.validateToken(accessToken)) {
      return "redirect:/error/403";
    }
    log.debug(accessToken);

    // DB에서 단과대 목록 조회
    List<Faculty> faculties = adminApiService.getAllFaculties().getFaculties();
    model.addAttribute("faculties", faculties);

    // notificationCategory
    List<NotificationCategory> categories = Arrays.stream(NotificationCategory.values()).toList();
    model.addAttribute("category", categories);

    return "admin/notification";
  }
}
