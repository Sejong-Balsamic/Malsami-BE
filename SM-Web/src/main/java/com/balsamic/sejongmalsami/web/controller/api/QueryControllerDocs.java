package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.application.dto.QueryCommand;
import com.balsamic.sejongmalsami.application.dto.QueryDto;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface QueryControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.26",
          author = Author.BAEKJIHOON,
          description = "검색 페이지 init"
      )
  })
  @Operation(
      summary = "통합 검색",
      description = """
      질문, 자료, 자료요청, 공지사항 게시글을 통합하여 검색합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - query (선택): 검색어 (제목 및 본문 검색)
      - subject (선택): 교과목명 필터링
      - sortType (선택): 정렬 기준 (기본값: LATEST)
        * LATEST: 최신순
        * MOST_LIKED: 좋아요순
        * COMMENT_COUNT: 댓글순
        * VIEW_COUNT: 조회순
        * OLDEST: 과거순
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지당 아이템 수 (기본값: 30)
      
      **응답 데이터**
      - QueryDto: 통합 검색 결과
        * questionPostsPage: 질문 게시글 결과
        * documentPostsPage: 자료 게시글 결과
        * documentRequestPostsPage: 자료요청 게시글 결과
        * noticePostsPage: 공지사항 게시글 결과
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      - BAD_REQUEST (400): 잘못된 요청 파라미터
      
      **참고사항**
      - 각 게시판별로 pageSize 만큼의 결과 반환 (최대 4 x pageSize 개 결과)
      - 검색어 미입력 시 전체 게시글 조회
      - 제목과 본문을 대상으로 검색 수행
      """
  )
  ResponseEntity<QueryDto> getPostsByQuery(
      QueryCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.02.21",
          author = Author.BAEKJIHOON,
          description = "인기 검색어 반환 init"
      )
  })
  @Operation(
      summary = "인기 검색어 조회",
      description = """
      사용자들이 가장 많이 검색한 인기 검색어 순위를 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - topN (선택): 조회할 상위 N개 인기 검색어 (기본값: 10)
      
      **응답 데이터**
      - QueryDto: 인기 검색어 정보
        * searchHistoryList: 인기 검색어 리스트
        * keyword: 검색어
        * searchCount: 검색 횟수
        * rank: 현재 순위
        * rankChange: 순위 변동
        * isNew: 신규 진입 여부
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      - BAD_REQUEST (400): 잘못된 topN 값
      
      **참고사항**
      - 30분마다 순위 업데이트됨
      - rankChange 양수: 순위 상승, 음수: 순위 하락
      - isNew=true: 순위권 신규 진입 검색어
      - 실시간 트렌드 파악을 위한 데이터 제공
      """
  )
  ResponseEntity<QueryDto> getTopKeywords(QueryCommand command);
}
