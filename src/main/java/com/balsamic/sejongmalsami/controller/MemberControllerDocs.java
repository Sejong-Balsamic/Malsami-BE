package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface MemberControllerDocs {

  @Operation(
      summary = "로그인 요청",
      description = """
    **로그인 요청**

    세종대학교 대양휴머니티 칼리지 로그인 기능을 제공합니다.

    **이 API는 인증이 필요하지 않으며, JWT 토큰 없이 접근 가능합니다**

    **입력 파라미터 값:**

    - **String sejongPortalId**: 세종대학교 포털 ID  
      _예: "18010561"_

    - **String sejongPortalPassword**: 세종대학교 포털 비밀번호  
      _예: "your_password"_

    **DB에 저장되는 학사 정보:**
    - **String studentName**: 학생 이름
    - **Long studentId**: 학번
    - **String major**: 전공
    - **String academicYear**: 학년
    - **String enrollmentStatus**: 현재 재학 여부

    **반환 파라미터 값:**

    - **MemberDto**: 로그인 및 인증이 완료된 회원의 정보와 토큰
      - **Member member**: 회원 정보
      - **String accessToken**: JWT 액세스 토큰 (인증된 사용자를 위한 토큰)
      - **String refreshToken**: JWT 리프레시 토큰 (새로운 액세스 토큰 발급을 위한 토큰)

    **참고 사항:**

    - 이 API를 통해 사용자는 세종대학교 포털 인증 정보를 이용하여 로그인할 수 있습니다.
    - 성공적인 인증 후, 시스템은 액세스 토큰과 리프레시 토큰을 발급하여 반환합니다.
    - 반환된 토큰은 향후 요청 시 인증 수단으로 사용됩니다.
    """
  )
  ResponseEntity<MemberDto> signIn(@ModelAttribute MemberCommand command);
}
