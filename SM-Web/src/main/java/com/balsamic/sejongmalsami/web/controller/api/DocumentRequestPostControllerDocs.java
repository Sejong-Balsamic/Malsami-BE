package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.post.dto.DocumentCommand;
import com.balsamic.sejongmalsami.post.dto.DocumentDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface DocumentRequestPostControllerDocs {
  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.18",
          author = Author.BAEKJIHOON,
          description = "자료요청 글 작성 init"
      )
  })
  @Operation(
      summary = "자료 요청 게시글 작성",
      description = """
      필요한 학습 자료를 요청하는 게시글을 작성합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER (중인 등급 이상, 엽전 1000냥 이상)
      
      **요청 파라미터**
      - title (필수): 자료 요청 게시글 제목
      - content (필수): 자료 요청 게시글 본문
      - subject (선택): 교과목명
      - documentTypes (선택): 요청하는 자료 유형 (최대 2개)
        * DOCUMENT: 필기 자료, 교안, 녹화본, 실험/실습 자료
        * PAST_EXAM: 퀴즈, 기출 문제, 과제
        * SOLUTION: 솔루션
      - isPrivate (선택): 내 정보 비공개 여부 (기본값: false)
      
      **응답 데이터**
      - DocumentDto: 생성된 자료 요청 게시글 정보
        * documentRequestPost: 자료 요청 게시글 상세 정보
        * isPrivate: 비공개 설정 여부
        * requestedTypes: 요청한 자료 유형
      
      **예외 상황**
      - INSUFFICIENT_YEOPJEON (403): 엽전 부족 (중인 등급 이상 필요)
      - BAD_REQUEST (400): 필수 필드 누락 또는 잘못된 데이터
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      
      **참고사항**
      - 자료 요청 게시판은 중인 등급 이상만 접근 가능
      - isPrivate=true 설정 시 댓글에 작성자 정보가 비공개 처리
      - 자료 제공자가 있을 경우 엽전 보상 시스템 존재
      """
  )
  ResponseEntity<DocumentDto> saveDocumentRequestPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "API 인증 생략"
      ),
      @ApiChangeLog(
          date = "2024.11.19",
          author = Author.BAEKJIHOON,
          description = "자료요청 글 필터링 조회 init"
      )
  })
  @Operation(
      summary = "자료 요청 글 필터링 조회",
      description = """
      다양한 조건으로 자료 요청 게시글을 필터링하여 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - subject (선택): 과목명 필터링
      - faculty (선택): 단과대 필터링
      - documentTypes (선택): 카테고리 필터링 (최대 2개)
        * DOCUMENT: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
        * PAST_EXAM: 퀴즈, 기출 문제, 과제 등
        * SOLUTION: 솔루션 등
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지 크기 (기본값: 30)
      
      **응답 데이터**
      - DocumentDto: 자료 요청 게시판 정보
        * documentRequestPostsPage: 필터링된 자료 요청 글 목록
      
      **예외 상황**
      - INVALID_PAGE_REQUEST (400): 잘못된 페이지 요청
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 인증 없이 접근 가능한 공개 API
      - 모든 필터링 미선택 시 전체 글이 최신순으로 조회
      - 여러 필터를 조합하여 원하는 자료 요청글 검색 가능
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      """
  )
  ResponseEntity<DocumentDto> getFilteredDocumentRequestPosts(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.19",
          author = Author.BAEKJIHOON,
          description = "자료요청 글 상세 조회 init"
      )
  })
  @Operation(
      summary = "자료 요청 상세 글 조회",
      description = """
      특정 자료 요청 게시글의 상세 정보를 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - documentPostId (필수): 조회할 자료 요청 글 ID
      
      **응답 데이터**
      - DocumentDto: 자료 요청 글 상세 정보
        * documentRequestPost: 자료 요청 글 상세 내용
      
      **예외 상황**
      - DOCUMENT_REQUEST_POST_NOT_FOUND (404): 자료 요청 글을 찾을 수 없음
      - UNAUTHORIZED (401): 인증이 필요함
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - API 요청 시 해당 글의 조회수가 증가
      - 자료 요청 글의 상세 내용과 관련 정보 제공
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      """
  )
  ResponseEntity<DocumentDto> getDocumentRequestPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);
}
