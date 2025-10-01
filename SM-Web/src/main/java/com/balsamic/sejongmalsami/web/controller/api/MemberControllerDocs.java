package com.balsamic.sejongmalsami.web.controller.api;

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
      summary = "마이페이지 조회",
      description = """
      사용자의 상세 정보, 통계, 랭킹 등을 포함한 마이페이지 데이터를 제공합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - MemberDto: 회원 상세 정보 및 통계
        * member: 기본 회원 정보
        * yeopjeon: 엽전 정보 및 랭킹
        * yeopjeonRank: 엽전 랭킹 순위
        * yeopjeonPercentile: 엽전 백분위
        * exp: 경험치 정보 및 레벨
        * expRank: 경험치 랭킹 순위
        * expPercentile: 경험치 백분위
        * questionPostCount: 작성한 질문 게시글 수
        * answerPostCount: 작성한 답변 게시글 수
        * documentPostCount: 작성한 자료 게시글 수
        * documentRequestPostCount: 작성한 자료요청 게시글 수
        * totalPostCount: 총 게시글 수
        * totalCommentCount: 총 댓글 수
        * totalPopularPostCount: 인기 자료 수
        * totalLikeCount: 받은 총 좋아요 수
      
      **예외 상황**
      - MEMBER_NOT_FOUND (404): 회원 정보를 찾을 수 없음
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      
      **참고사항**
      - 엽전과 경험치 랭킹은 전체 사용자 대비 계산됨
      - 백분위는 상위 몇 퍼센트에 위치하는지 표시
      - 자료게시판 접근권한 정보도 함께 제공
      """
  )
  ResponseEntity<MemberDto> myPage(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.08.10",
          author = Author.SUHSAECHAN,
          description = "MemberCommand 롤백"
      ),
      @ApiChangeLog(
          date = "2025.08.10",
          author = Author.SUHSAECHAN,
          description = "MemberCommand 제거, 요청 파라미터를 받지않음"
      ),
      @ApiChangeLog(
          date = "2024.11.30",
          author = Author.SUHSAECHAN,
          description = "마이페이지 API init"
      )
  })
  @Operation(
      summary = "내 기본 정보 조회",
      description = """
      사용자의 기본 정보만 반환하는 경량 API입니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - MemberDto: 회원 기본 정보
        * member: 회원 기본 정보 (닉네임, 이메일, 학과 등)
      
      **예외 상황**
      - MEMBER_NOT_FOUND (404): 회원 정보를 찾을 수 없음
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      
      **참고사항**
      - 마이페이지 대비 경량화된 버전으로 빠른 응답 제공
      - 통계나 랭킹 정보는 포함되지 않음
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
      summary = "자료게시판 접근권한 조회",
      description = """
      사용자의 엽전에 따른 자료게시판 및 각 등급별 접근 가능 여부를 확인합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - MemberDto: 자료게시판 접근 권한 정보
        * canAccessCheonmin: 천민 게시판 접근 가능 여부
        * canAccessJungin: 중인 게시판 접근 가능 여부
        * canAccessYangban: 양반 게시판 접근 가능 여부
        * canAccessKing: 왕 게시판 접근 가능 여부
        * yeopjeon: 현재 사용자의 엽전
        * cheonminRequirement: 천민 게시판 요구 엽전 (0냥)
        * junginRequirement: 중인 게시판 요구 엽전 (1000냥)
        * yangbanRequirement: 양반 게시판 요구 엽전 (5000냥)
        * kingRequirement: 왕 게시판 요구 엽전 (10000냥)
      
      **예외 상황**
      - MEMBER_NOT_FOUND (404): 회원 정보를 찾을 수 없음
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      
      **참고사항**
      - 엽전에 따라 자료게시판 등급별 접근 권한이 결정됨
      - 높은 등급의 자료일수록 더 좋은 품질의 자료 제공
      """
  )
  ResponseEntity<MemberDto> getDocumentBoardAccessByTier(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);


}
