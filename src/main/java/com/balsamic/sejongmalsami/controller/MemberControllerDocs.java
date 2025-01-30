package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface MemberControllerDocs {

  @ApiChangeLogs({
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
      @ModelAttribute MemberCommand command,
      HttpServletResponse response);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.12.14",
          author = Author.SUHSAECHAN,
          description = "자료게시판 접근권한 정보 반환 로직 추가"
      ),
      @ApiChangeLog(
          date = "2024.11.28",
          author = Author.SUHSAECHAN,
          description = "마이페이지 엽전, 경험치, 백분율, 전체적인 총 개수 반환"
      ),
      @ApiChangeLog(
          date = "2024.10.28",
          author = Author.SUHSAECHAN,
          description = "마이페이지 API init"
      )
  })
  @Operation(
      summary = "마이페이지",
      description = """
        **마이페이지 조회**

        **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다**

        ### **요청 파라미터**
        - **없음**

        ### **반환 파라미터 값**

        - **`MemberDto`**: 회원의 상세 정보 및 통계
          - **`Member member`** : 회원 정보
          - **`Yeopjeon yeopjeon`** : 엽전 정보
          - **`int yeopjeonRank`**: 엽전 랭킹
          - **`int totalYeopjeonMembers`**: 총 (엽전을가진) 사람수
          - **`double yeopjeonPercentile`**: 엽전 백분위
          - **`Exp exp`** : 경험치 정보
          - **`int expRank`**: 경험치 랭킹
          - **`int totalExpMembers`**: 총 (경험치를가진) 사람수 (전체 회원수)
          - **`double expPercentile`**: 경험치 백분위
          - **`long questionPostCount`**: 질문 게시글 수
          - **`long answerPostCount`**: 답변 게시글 수
          - **`long documentPostCount`**: 문서 게시글 수
          - **`long documentRequestPostCount`**: 문서 요청 게시글 수
          - **`long totalPostCount`**: 총 게시글 수
          - **`long totalCommentCount`**: 총 댓글 수
          - **`long totalPopularPostCount`**: 총 인기자료 수
          - **`long totalLikeCount`**: 총 좋아요 수

        """
  )
  ResponseEntity<MemberDto> myPage(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.30",
          author = Author.SUHSAECHAN,
          description = "마이페이지 API init"
      )
  })
  @Operation(
      summary = "내정보 반환",
      description = """
        **마이페이지 조회**

        **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다**

        ### **요청 파라미터**
        - **없음**

        ### **반환 파라미터 값**

        - **`MemberDto`**: 회원의 상세 정보 및 통계
          - **`Member member`** : 회원 정보
        """
  )
  ResponseEntity<MemberDto> myInfo(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.30",
          author = Author.SUHSAECHAN,
          description = "자료게시판 접근권한 정보 반환 API : #521"
      )
  })
  @Operation(
      summary = "자료게시판 접근권한 정보 반환",
      description = """
        **자료게시판 접근권한 정보 반환**

        **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다**

        - **`MemberDto`**: 회원의 상세 정보 및 접근 권한
          - **`boolean canAccessCheonmin`**: 천민 게시판 접근 가능 여부
          - **`boolean canAccessJungin`**: 중인 게시판 접근 가능 여부
          - **`boolean canAccessYangban`**: 양반 게시판 접근 가능 여부
          - **`boolean canAccessKing`**: 왕 게시판 접근 가능 여부
          - **`Integer yeopjeon`**: 현재 회원의 엽전값
          - **`Integer cheonminRequirement`**: 천민 게시판 접근 요구 엽전
          - **`Integer junginRequirement`**: 중인 게시판 접근 요구 엽전
          - **`Integer yangbanRequirement`**: 양반 게시판 접근 요구 엽전
          - **`Integer kingRequirement`**: 왕 게시판 접근 요구 엽전
        """
  )
  ResponseEntity<MemberDto> getDocumentBoardAccessByTier(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.01.30",
          author = Author.SUHSAECHAN,
          description = "반환값 수정 Object -> MemberDto"
      ),
      @ApiChangeLog(
          date = "2024.12.26",
          author = Author.BAEKJIHOON,
          description = "내가 작성한 글"
      )
  })
  @Operation(
      summary = "내가 작성한 글 조회",
      description = """
        **내가 작성한 질문/답변/자료/자료요청 글 조회**

        **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다**

        ### 요청 파라미터
        - **contentType**: 조회하고자 하는 글의 유형
          - QUESTION, ANSWER, DOCUMENT, DOCUMENT_REQUEST
        - **sortType**: 정렬 조건
          - LATEST, OLDEST, MOST_LIKED, VIEW_COUNT
        - **pageNumber**: 페이지 번호
        - **pageSize**: 페이지당 보여줄 글 개수
                           
        ### 반환값
        #### contentType = `QUESTION`
        - **`MemberDto`**: 질문 게시판 정보 반환
          - **`Page<QuestionPost> questionPostsPage`**: 내가 작성한 질문글
        #### contentType = `ANSWER`
        - **`MemberDto`**: 질문 게시판 정보 반환
          - **`Page<QuestionPost> questionPostsPage`**: 내가 답변을 작성한 질문글
        #### contentType = `DOCUMENT`
        - **`MemberDto`**: 자료 게시판 정보 반환
          - **`Page<DocumentPost> documentPostsPage`**: 내가 작성한 자료글
        #### contentType = `DOCUMENT_REQUEST`
        - **`MemberDto`**: 자료 게시판 정보 반환
          - **`Page<DocumentPost> documentRequestPostsPage`**: 내가 작성한 자료 요청글
          """
  )
  ResponseEntity<MemberDto> getAllMemberPost(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);
}
