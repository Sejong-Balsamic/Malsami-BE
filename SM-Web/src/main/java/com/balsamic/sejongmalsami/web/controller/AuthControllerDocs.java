package com.balsamic.sejongmalsami.web.controller;

import com.balsamic.sejongmalsami.auth.dto.AuthCommand;
import com.balsamic.sejongmalsami.auth.dto.AuthDto;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.member.dto.MemberDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface AuthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.07.30",
          author = Author.SUHSAECHAN,
          description = "요청 파라미터 객체 변경: MemberCommand -> AuthCommand"
      ),
      @ApiChangeLog(
          date = "2025.07.28",
          author = Author.BAEKJIHOON,
          description = "API 경로 변경: /api/member/signin -> /api/auth/signin"
      ),
      @ApiChangeLog(
          date = "2024.10.29",
          author = Author.SUHSAECHAN,
          description = "엽전, 경험치 생성, Member의 isFirstLogin 전달X (JsonIgnore)"
      ),
      @ApiChangeLog(
          date = "2024.10.04",
          author = Author.SUHSAECHAN,
          description = "Samesite 수정: Strict -> None 크로스사이트 요청 허용"
      ),
      @ApiChangeLog(
          date = "2024.09.25",
          author = Author.SUHSAECHAN,
          description = "로그인 토큰 추가 ( Access, Refresh )"
      ),
      @ApiChangeLog(
          date = "2024.08.10",
          author = Author.SUHSAECHAN,
          description = "세종대학교 로그인 기능 구현"
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
            
            - **MemberDto**: 로그인 및 인증이 완료된 회원의 정보와 토큰
              - **Member member**: 회원 정보
              - **String accessToken**: JWT 액세스 토큰 (인증된 회원을 위한 토큰)
              - **Boolean isFirstLogin**: 첫 로그인 여부
              - **Yeopjeon yeopjeon**: 엽전 정보 (첫 로그인 시 지급된 엽전)
              - **Exp exp**: 경험치 정보
            
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
            
            - 이 API를 통해 회원은 세종대학교 포털 인증 정보를 이용하여 로그인할 수 있습니다.
            - 성공적인 인증 후, 시스템은 액세스 토큰과 리프레시 토큰을 발급하여 반환합니다.
            - 액세스 토큰은 클라이언트에서 인증이 필요한 API 요청 시 사용되며, 리프레시 토큰은 새로운 액세스 토큰을 발급받기 위해 서버에 저장됩니다.
            - 리프레시 토큰은 클라이언트에서 직접 접근할 수 없도록 HTTP-Only 쿠키로 설정되어 보안이 강화됩니다.
            - 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급받는 API는 `/api/auth/refresh` 엔드포인트를 사용합니다.
            """
  )
  ResponseEntity<MemberDto> signIn(
      @ModelAttribute AuthCommand command,
      HttpServletResponse response);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.07.28",
          author = Author.BAEKJIHOON,
          description = "API 경로 변경: /api/member/mobile/signin -> /api/auth/mobile/signin"
      ),
      @ApiChangeLog(
          date = "2025.03.22",
          author = Author.SUHSAECHAN,
          description = "모바일 전용 로그인 API 추가 (쿠키 대신 토큰 직접 반환)"
      )
  })
  @Operation(
      summary = "모바일 전용 로그인 요청",
      description = """
            **모바일 전용 로그인 요청**
            
            모바일 클라이언트를 위한 전용 로그인 API입니다.
            웹과 달리 쿠키를 사용하지 않고 토큰을 직접 반환합니다.
            
            **이 API는 인증이 필요하지 않으며, JWT 토큰 없이 접근 가능합니다**
            
            **입력 파라미터 값:**
            
            - **String sejongPortalId**: 세종대학교 포털 ID [필수]
            - **String sejongPortalPassword**: 세종대학교 포털 비밀번호 [필수]
            
            **반환 파라미터 값:**
            
            - **Member member**: 회원 정보
            - **String accessToken**: JWT 액세스 토큰 (API 인증용)
            - **String refreshToken**: JWT 리프레시 토큰 (토큰 갱신용)
            - **Boolean isFirstLogin**: 첫 로그인 여부
            - **Boolean isAdmin**: 관리자 권한 여부
            - **Yeopjeon yeopjeon**: 엽전 정보 (첫 로그인 시에만)
            - **Exp exp**: 경험치 정보
            
            **응답 코드:**
            
            - **200 OK**: 로그인 성공
            - **401 Unauthorized**: 세종포털 인증 실패
            - **500 Internal Server Error**: 서버 내부 오류
            
            **주의사항:**
            
            - 모바일에서는 쿠키를 사용하지 않으므로 refreshToken을 안전하게 저장해야 합니다.
            - accessToken은 Authorization 헤더에 'Bearer {token}' 형식으로 사용합니다.
            - refreshToken은 만료 시 새로운 토큰 발급에 사용합니다.
            """
  )
  ResponseEntity<MemberDto> signInForMobile(AuthCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.27",
          author = Author.SUHSAECHAN,
          description = "반환값에 memberId 추가"
      ),
      @ApiChangeLog(
          date = "2024.11.24",
          author = Author.SUHSAECHAN,
          description = "반환값에 studentName 추가"
      ),
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
          - **String studentName**: 학생 이름 반환

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

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.09",
          author = Author.SUHSAECHAN,
          description = "요청값 객체 수정 , 기존 FcmCommand -> AuthCommand"
      ),
      @ApiChangeLog(
          date = "2025.01.23",
          author = Author.BAEKJIHOON,
          description = "로그아웃 시 FCM 토큰 삭제"
      ),
      @ApiChangeLog(
          date = "2024.11.24",
          author = Author.SUHSAECHAN,
          description = "버그수정 : #424 : 리프레시 토큰 삭제 로직 추가 및 SameSite 속성 설정"
      ),
      @ApiChangeLog(
          date = "2024.11.10",
          author = Author.SUHSAECHAN,
          description = "로그아웃 API 구현"
      )
  })
  @Operation(
      summary = "로그아웃 API",
      description = """
        **로그아웃 API**

        클라이언트는 이 API를 호출하여 사용자 세션을 종료할 수 있습니다.
        로그아웃 시, 서버는 `refreshToken` 쿠키를 삭제하고, 서버 측에서도 리프레시 토큰을 제거합니다.
        또한 formData로 입력된 fcmToken을 데이터베이스에서 삭제합니다.

        **입력 파라미터 값:**
        
        - **String fcmToken**: 사용자의 fcmToken

        - **Cookie**: 리프레시 토큰이 포함된 HTTP-Only 쿠키
          - **Name:** `refreshToken`
          - **Value:** 저장된 리프레시 토큰 값

        **반환 파라미터 값:**

        - **없음**: 성공 시 200 OK 응답

        **응답 코드:**

        - **200 OK**: 로그아웃 성공
        - **401 Unauthorized**: 리프레시 토큰이 유효하지 않거나 만료됨
        - **403 Forbidden**: 쿠키에서 리프레시 토큰을 찾을 수 없음
        """
  )
  ResponseEntity<Void> logout(
      CustomUserDetails customUserDetails,
      AuthCommand command,
      HttpServletRequest request,
      HttpServletResponse response);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.09",
          author = Author.SUHSAECHAN,
          description = "요청값, 응답값 객체 수정 , 기존 Fcm Command, Dto -> Auth Command, Dto"
      ),
      @ApiChangeLog(
          date = "2025.01.15",
          author = Author.BAEKJIHOON,
          description = "FCM 토큰 저장"
      )
  })
  @Operation(
      summary = "FCM 토큰 저장",
      description = """
          **FCM 토큰 저장**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **String fcmToken**: Firebase에서 발급받은 토큰 [필수]

          **반환 파라미터 값:**

          - **FcmDto**: FCM 정보
            - **FcmToken fcmToken**: FCM 토큰 정보
          """
  )
  ResponseEntity<AuthDto> saveFcmToken(
      CustomUserDetails customUserDetails,
      AuthCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.22",
          author = Author.SUHSAECHAN,
          description = "모바일 전용 토큰 갱신 API 추가 (Access + Refresh 둘 다 재발급)"
      )
  })
  @Operation(
      summary = "모바일 전용 토큰 갱신 (Access + Refresh 둘 다 재발급)",
      description = """
          **모바일 전용 토큰 갱신 API**

          모바일 클라이언트에서 사용하는 전용 토큰 갱신 API입니다.
          기존 리프레시 토큰을 전달하면 새로운 액세스 토큰과 리프레시 토큰을 모두 재발급합니다.

          **이 API는 인증이 필요하지 않으며, refreshToken만으로 접근 가능합니다.**

          **입력 파라미터 값:**

          - **String refreshToken**: 기존 리프레시 토큰 [필수]

          **반환 파라미터 값:**

          - **String accessToken**: 새로운 JWT 액세스 토큰
          - **String refreshToken**: 새로운 JWT 리프레시 토큰
          - **String studentName**: 학생 이름
          - **UUID memberId**: 회원 ID

          **응답 코드:**

          - **200 OK**: 토큰 갱신 성공
          - **401 Unauthorized**: 리프레시 토큰이 유효하지 않거나 만료됨
          - **400 Bad Request**: 리프레시 토큰 누락

          **주의사항:**

          - 모바일 환경에서는 쿠키를 사용하지 않으므로 리프레시 토큰을 직접 전달받습니다.
          - 기존 리프레시 토큰은 무효화되고 새로운 리프레시 토큰이 발급됩니다.
          """
  )
  ResponseEntity<AuthDto> refreshTokensForMobile(AuthCommand command);
}
