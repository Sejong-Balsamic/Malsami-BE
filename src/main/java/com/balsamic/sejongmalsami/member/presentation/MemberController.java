package com.balsamic.sejongmalsami.member.presentation;

import com.balsamic.sejongmalsami.common.auth.application.SejongStudentAuthService;
import com.balsamic.sejongmalsami.common.auth.dto.request.SejongStudentAuthRequest;
import com.balsamic.sejongmalsami.common.auth.dto.response.SejongStudentAuthResponse;
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
  @Operation(summary = "로그인 요청", description = ""
      + "세종대학교 대양휴머니티 칼리지 로그인 기능\n\n"
      + "DB에 저장하는 정보는 = '이름, 학번, 전공, 학년, 현재 재학여부' 입니다.")
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
