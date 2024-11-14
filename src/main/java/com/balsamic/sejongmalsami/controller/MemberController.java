package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(
    name = "회원 관리 API",
    description = "회원 관리 API 제공"
)
public class MemberController implements MemberControllerDocs {

  private final MemberService memberService;

  @Override
  @PostMapping(value = "/signin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> signIn(
      @ModelAttribute MemberCommand command, HttpServletResponse response) {
    return ResponseEntity.ok(memberService.signIn(command, response));
  }

  @Override
  @PostMapping(value = "/my-page", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> myPage(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    command.setMemberId(customUserDetails.getMemberId());
    return ResponseEntity.ok(memberService.myPage(command));
  }
}