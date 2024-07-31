package com.balsamic.sejongmalsami.member.presentation;

import com.balsamic.sejongmalsami.common.config.auth.application.SejongStudentAuthService;
import com.balsamic.sejongmalsami.common.config.auth.dto.request.SejongStudentAuthRequest;
import com.balsamic.sejongmalsami.common.config.auth.dto.response.SejongStudentAuthResponse;
import com.balsamic.sejongmalsami.member.application.MemberService;
import com.balsamic.sejongmalsami.member.dto.response.CreateMemberResponse;
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
  private final SejongStudentAuthService sejongStudentAuthService;
  private final MemberService memberService;

  @PostMapping("/sign-in")
  @Operation(summary = "로그인 요청", description = "로그인 요청 기능\n\n 이메일 비밀번호는 필수\n\n 기기 Id 안 보내면 db에 등록 안 되어서 간소 로그인 불가")
  public ResponseEntity<CreateMemberResponse> signIn(
      @RequestBody SejongStudentAuthRequest sejongStudentAuthRequest
  ) throws IOException {
    SejongStudentAuthResponse sejongStudentAuthResponse
        = sejongStudentAuthService.getMemberAuthInfos(sejongStudentAuthRequest);
    CreateMemberResponse response = memberService.createMember(
        sejongStudentAuthResponse);

    return ResponseEntity.ok(response);
  }

}
