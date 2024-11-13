package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.WebLoginDto;
import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/web/auth")
@RequiredArgsConstructor
@Slf4j
public class WebAuthController {

  private final MemberService memberService;
  private final JwtUtil jwtUtil;

  @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseBody
  public WebLoginDto login(MemberCommand command, HttpServletResponse response) {
    log.info("관리자 로그인 시도: {}", command.getSejongPortalId());
    try {
      MemberDto memberDto = memberService.signIn(command, response);
      log.info("로그인 결과: isAdmin={}, accessToken={}", memberDto.getIsAdmin(), memberDto.getAccessToken());

      if (!memberDto.getIsAdmin()) {
        log.warn("관리자 권한 없음: {}", command.getSejongPortalId());
        return WebLoginDto.builder()
            .success(false)
            .message("로그인에 실패했습니다. 관리자가 아닙니다")
            .build();
      }

      log.info("관리자 로그인 성공: {}", command.getSejongPortalId());
      return WebLoginDto.builder()
          .success(true)
          .accessToken(memberDto.getAccessToken())
          .build();

    } catch (Exception e) {
      log.error("로그인 실패: {}", command.getSejongPortalId(), e);
      return WebLoginDto.builder()
          .success(false)
          .message("로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.")
          .build();
    }
  }
}