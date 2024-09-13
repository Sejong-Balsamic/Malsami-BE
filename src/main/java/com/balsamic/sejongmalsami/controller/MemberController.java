package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(
    name = "회원 관리 API",
    description = "회원 관리 API 제공"
)
public class MemberController {
  private final SejongPortalAuthenticator sejongPortalAuthenticator;
  private final MemberService memberService;

  @PostMapping(value = "/signin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "로그인 요청",
      description = """
      **로그인 요청**

      세종대학교 대양휴머니티 칼리지 로그인 기능을 제공합니다.

      **입력 파라미터 값:**

      - **`String sejongPortalId`**: 세종대학교 포털 ID  
        _예: "18010561"_

      - **`String sejongPortalPassword`**: 세종대학교 포털 비밀번호  
        _예: "your_password"_

      **DB에 저장되는 정보:**

      - **`String studentName`**: 학생 이름
      - **`String studentId`**: 학번
      - **`String major`**: 전공
      - **`String academicYear`**: 학년
      - **`String enrollmentStatus`**: 현재 재학 여부

      **반환 파라미터 값:**

      - **`MemberDto member`**: 로그인 및 인증이 완료된 회원의 정보
      """
  )
  public ResponseEntity<MemberDto> signIn(
      @ModelAttribute MemberCommand command) throws IOException {
    return ResponseEntity.ok(memberService.createMember(command));
  }
}
