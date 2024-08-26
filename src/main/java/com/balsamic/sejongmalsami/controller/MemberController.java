package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
  private final SejongPortalAuthenticator sejongPortalAuthenticator;
  private final MemberService memberService;

  @PostMapping("/sign-in")
  @Operation(summary = "로그인 요청", description = ""
      + "세종대학교 대양휴머니티 칼리지 로그인 기능\n\n"
      + "DB에 저장하는 정보는 = '이름, 학번, 전공, 학년, 현재 재학여부' 입니다.")
  public ResponseEntity<MemberDto> signIn(
      @RequestBody MemberCommand command) throws IOException {
    return ResponseEntity.ok(memberService.createMember(command));
  }
}
