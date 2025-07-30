package com.balsamic.sejongmalsami.web.controller;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.member.dto.MemberCommand;
import com.balsamic.sejongmalsami.member.dto.MemberDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface MemberControllerDocs {

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


}
