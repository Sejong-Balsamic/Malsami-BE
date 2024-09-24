package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface MemberControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.09.25",
          author = Author.SUHSAECHAN,
          description = "로그인 토큰 추가 ( Access, Refresh )"
      ),
      @ApiChangeLog(
          date = "2024.08.10",
          author = Author.SUHSAECHAN,
          description = "API 최초 작성"
      )
  })
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

    - **MemberDto**: 로그인 및 인증이 완료된 회원의 정보와 액세스 토큰
      - **Member member**: 회원 정보
      - **String accessToken**: JWT 액세스 토큰 (인증된 사용자를 위한 토큰)

    **추가로, 리프레시 토큰은 HTTP-Only 쿠키로 설정되어 반환됩니다:**

    - **Set-Cookie**: `refreshToken` 쿠키가 HTTP-Only 속성으로 설정되어 전송됩니다.
      - **Name:** `refreshToken`
      - **Value:** JWT 리프레시 토큰
      - **Path:** `/api/auth/refresh`
      - **HttpOnly:** `true`
      - **Secure:** `false` (개발 환경), `true` (배포 환경)
      - **Max-Age:** 7일

    **토큰 만료 시간:**

    - **Access Token (accessToken):** 1시간
    - **Refresh Token (refreshToken):** 7일

    **참고 사항:**

    - 이 API를 통해 사용자는 세종대학교 포털 인증 정보를 이용하여 로그인할 수 있습니다.
    - 성공적인 인증 후, 시스템은 액세스 토큰과 리프레시 토큰을 발급하여 반환합니다.
    - 액세스 토큰은 클라이언트에서 인증이 필요한 API 요청 시 사용되며, 리프레시 토큰은 새로운 액세스 토큰을 발급받기 위해 서버에 저장됩니다.
    - 리프레시 토큰은 클라이언트에서 직접 접근할 수 없도록 HTTP-Only 쿠키로 설정되어 보안이 강화됩니다.
    - 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받는 API는 `/api/auth/refresh` 엔드포인트를 사용합니다.
    """
  )
  ResponseEntity<MemberDto> signIn(
      @ModelAttribute MemberCommand command,
      HttpServletResponse response);
}
