package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.AuthDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AuthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.01",
          author = Author.SUHSAECHAN,
          description = "DOCS 추가 및 응답코드 세분화"
      ),
      @ApiChangeLog(
          date = "2024.10.01",
          author = Author.SUHSAECHAN,
          description = "AccessToken 재발급 API 구현"
      )
  })
  @Operation(
      summary = "액세스 토큰 재발급 요청",
      description = """
          **액세스 토큰 재발급 요청**

          클라이언트는 HTTP-Only 쿠키로 저장된 리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급받을 수 있습니다.

          **이 API는 인증이 필요하지 않으며, refreshToken만으로 접근 가능합니다.**

          **입력 파라미터 값:**

          - **Cookie**: 리프레시 토큰이 포함된 HTTP-Only 쿠키
            - **Name:** `refreshToken`
            - **Value:** 저장된 리프레시 토큰 값

          **반환 파라미터 값:**

          - **String accessToken**: 새로운 JWT 액세스 토큰 (인증이 필요한 요청에 사용)

          **예시:**

          **참고 사항:**

          - 이 API는 리프레시 토큰의 유효성을 검증한 후 새로운 액세스 토큰을 발급합니다.
          - 리프레시 토큰은 쿠키로 저장되며, 클라이언트에서 직접 접근할 수 없으므로, 쿠키는 자동으로 서버로 전송됩니다.
          - 새로운 액세스 토큰은 반환된 후, 클라이언트는 이를 사용하여 인증이 필요한 API 요청에 사용할 수 있습니다.
          - 리프레시 토큰이 만료되었거나 유효하지 않을 경우, 서버에서 401 Unauthorized 상태 코드가 반환되며, 클라이언트는 사용자를 다시 로그인시켜야 합니다.

          **응답 코드:**

          - **200 OK**: 새로운 액세스 토큰 발급 성공
          - **401 Unauthorized**: 리프레시 토큰이 유효하지 않거나 만료됨
          - **400 Bad Request**: 쿠키에서 리프레시 토큰을 찾을 수 없음

          **추가 설명:**

          - 리프레시 토큰을 이용한 액세스 토큰 재발급은 보안을 강화하는 방법으로, 클라이언트가 리프레시 토큰을 저장할 필요가 없습니다.
          - 리프레시 토큰은 자동으로 쿠키로 전송되며, 쿠키는 HTTP-Only 속성으로 설정되어 있기 때문에 클라이언트에서 접근할 수 없습니다.
          """
  )
  ResponseEntity<AuthDto> refreshAccessToken(HttpServletRequest request);
}

