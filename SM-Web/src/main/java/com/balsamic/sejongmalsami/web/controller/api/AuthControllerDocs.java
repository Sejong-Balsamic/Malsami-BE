package com.balsamic.sejongmalsami.web.controller.api;

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
          date = "2025.08.08",
          author = Author.SUHSAECHAN,
          description = "반환 타입 변경: MemberDto -> AuthDto, 불필요한 엽전/경험치 정보 제외, 필요한 isFirstLogin과 isAdmin 정보 유지"
      ),
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
      summary = "세종대학교 포털 로그인",
      description = """
      세종대학교 포털 인증 정보를 통한 로그인 기능입니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - sejongPortalId (필수): 세종대학교 포털 ID
      - sejongPortalPassword (필수): 세종대학교 포털 비밀번호
      
      **응답 데이터**
      - AuthDto: 인증 완료된 사용자 정보
        * accessToken: JWT 액세스 토큰 (유효기간: 1시간)
        * refreshToken: JWT 리프레시 토큰 (모바일에서만 반환)
        * studentName: 학생 이름
        * memberId: 회원 고유 ID
        * isFirstLogin: 첫 로그인 여부
        * isAdmin: 관리자 권한 여부
      
      **예외 상황**
      - AUTHENTICATION_FAILED (401): 세종포털 인증 실패
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 리프레시 토큰은 HTTP-Only 쿠키로 자동 설정됩니다
      - 쿠키 경로: /api/auth/refresh, 유효기간: 7일
      - 로그인 시 학생 정보가 DB에 저장됩니다
      """
  )
  ResponseEntity<AuthDto> signIn(
      @ModelAttribute AuthCommand command,
      HttpServletResponse response);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.08.08",
          author = Author.SUHSAECHAN,
          description = "반환 타입 변경: MemberDto -> AuthDto, 불필요한 엽전/경험치 정보 제외, 필요한 isFirstLogin과 isAdmin 정보 유지"
      ),
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
      summary = "모바일 전용 세종대학교 포털 로그인",
      description = """
      모바일 클라이언트를 위한 로그인 API로, 쿠키 대신 토큰을 직접 반환합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - sejongPortalId (필수): 세종대학교 포털 ID
      - sejongPortalPassword (필수): 세종대학교 포털 비밀번호
      
      **응답 데이터**
      - AuthDto: 인증 완료된 사용자 정보
        * accessToken: JWT 액세스 토큰 (API 인증용)
        * refreshToken: JWT 리프레시 토큰 (토큰 갱신용)
        * studentName: 학생 이름
        * memberId: 회원 고유 ID
        * isFirstLogin: 첫 로그인 여부
        * isAdmin: 관리자 권한 여부
      
      **예외 상황**
      - AUTHENTICATION_FAILED (401): 세종포털 인증 실패
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 모바일 환경에서는 쿠키를 사용하지 않습니다
      - refreshToken은 클라이언트에서 안전하게 저장해야 합니다
      - accessToken은 Authorization 헤더에 'Bearer {token}' 형식으로 사용합니다
      """
  )
  ResponseEntity<AuthDto> signInForMobile(AuthCommand command);

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
      summary = "액세스 토큰 재발급",
      description = """
      HTTP-Only 쿠키의 리프레시 토큰을 이용하여 새로운 액세스 토큰을 발급받습니다.
      
      **인증 요구사항**
      - 인증 필요: 없음 (리프레시 토큰 쿠키만 필요)
      - 권한: 공개 API
      
      **요청 파라미터**
      - Cookie (필수): HTTP-Only 쿠키의 refreshToken
        * Name: refreshToken
        * Value: 유효한 리프레시 토큰
      
      **응답 데이터**
      - AuthDto: 새로 발급된 토큰 정보
        * accessToken: 새로운 JWT 액세스 토큰
        * studentName: 학생 이름
      
      **예외 상황**
      - TOKEN_NOT_FOUND (400): 쿠키에서 리프레시 토큰을 찾을 수 없음
      - TOKEN_EXPIRED (401): 리프레시 토큰이 만료됨
      - TOKEN_INVALID (401): 리프레시 토큰이 유효하지 않음
      
      **참고사항**
      - 리프레시 토큰 쿠키는 자동으로 서버에 전송됩니다
      - 토큰 만료 시 재로그인이 필요합니다
      - HTTP-Only 쿠키로 인해 클라이언트에서 직접 접근할 수 없습니다
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
      summary = "사용자 로그아웃",
      description = """
      사용자 세션을 종료하고 토큰 및 FCM 토큰을 삭제합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - fcmToken (선택): Firebase Cloud Messaging 토큰
      - Cookie: HTTP-Only 쿠키의 refreshToken
        * Name: refreshToken
        * Value: 저장된 리프레시 토큰
      
      **응답 데이터**
      - 없음 (성공 시 200 OK)
      
      **예외 상황**
      - TOKEN_INVALID (401): 리프레시 토큰이 유효하지 않거나 만료됨
      - TOKEN_NOT_FOUND (403): 쿠키에서 리프레시 토큰을 찾을 수 없음
      
      **참고사항**
      - 서버에서 리프레시 토큰 쿠키를 삭제합니다
      - 서버 측 리프레시 토큰도 함께 제거됩니다
      - FCM 토큰이 제공되면 데이터베이스에서 삭제합니다
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
      Firebase Cloud Messaging 토큰을 사용자 계정에 저장합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - fcmToken (필수): Firebase에서 발급받은 FCM 토큰
      
      **응답 데이터**
      - AuthDto: FCM 토큰 저장 정보
        * fcmToken: 저장된 FCM 토큰 정보
      
      **예외 상황**
      - UNAUTHORIZED (401): JWT 토큰이 없거나 유효하지 않음
      - BAD_REQUEST (400): FCM 토큰이 누락되거나 형식이 잘못됨
      
      **참고사항**
      - 푸시 알림 수신을 위해 필요한 토큰입니다
      - 기존 FCM 토큰이 있을 경우 새 토큰으로 대체됩니다
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
      summary = "모바일 전용 토큰 갱신",
      description = """
      모바일 클라이언트를 위한 액세스 토큰과 리프레시 토큰 동시 재발급 API입니다.
      
      **인증 요구사항**
      - 인증 필요: 없음 (리프레시 토큰만 필요)
      - 권한: 공개 API
      
      **요청 파라미터**
      - refreshToken (필수): 기존 리프레시 토큰
      
      **응답 데이터**
      - AuthDto: 새로 발급된 토큰 정보
        * accessToken: 새로운 JWT 액세스 토큰
        * refreshToken: 새로운 JWT 리프레시 토큰
        * studentName: 학생 이름
        * memberId: 회원 고유 ID
      
      **예외 상황**
      - TOKEN_MISSING (400): 리프레시 토큰이 누락됨
      - TOKEN_EXPIRED (401): 리프레시 토큰이 만료됨
      - TOKEN_INVALID (401): 리프레시 토큰이 유효하지 않음
      
      **참고사항**
      - 모바일 환경에서는 쿠키를 사용하지 않습니다
      - 기존 리프레시 토큰은 무효화되고 새 토큰이 발급됩니다
      - 토큰 로테이션을 통해 보안성을 향상시킵니다
      """
  )
  ResponseEntity<AuthDto> refreshTokensForMobile(AuthCommand command);
}
