package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.notice.dto.NoticePostCommand;
import com.balsamic.sejongmalsami.notice.dto.NoticePostDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface NoticePostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.1.2",
          author = Author.BAEKJIHOON,
          description = "공지사항 글 필터링 조회"
      )
  })
  @Operation(
      summary = "공지사항 검색",
      description = """
      공지사항 제목을 기반으로 검색하여 관련 공지사항을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - query (선택): 공지사항 제목 검색어
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지당 아이템 수 (기본값: 20)
      
      **응답 데이터**
      - NoticePostDto: 공지사항 검색 결과
        * noticePostsPage: 검색 조건에 맞는 공지사항 목록
        * totalElements: 총 검색 결과 수
        * totalPages: 총 페이지 수
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      - BAD_REQUEST (400): 잘못된 요청 파라미터
      
      **참고사항**
      - 검색어 미입력 시 전체 공지사항 조회
      - 제목에 검색어가 포함된 공지사항만 반환
      """
  )
  ResponseEntity<NoticePostDto> getFilteredNoticePosts(
      CustomUserDetails customUserDetails,
      NoticePostCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.6.26",
          author = Author.BAEKJIHOON,
          description = "PIN 된 공지사항 리스트 조회"
      )
  })
  @Operation(
      summary = "고정 공지사항 조회",
      description = """
      중요한 공지사항으로 고정(PIN)된 공지사항 목록을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - NoticePostDto: 고정 공지사항 정보
        * noticePosts: PIN된 공지사항 목록
        * isPinned: 고정 상태 여부
        * priority: 고정 우선순위
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 고정된 공지사항은 일반 공지사항보다 상단에 항상 표시
      - 관리자가 직접 설정한 중요 공지사항만 포함
      """
  )
  ResponseEntity<NoticePostDto> getPinnedNoticePost();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.9.6",
          author = Author.SUHSAECHAN,
          description = "공지사항 단일 글 조회 #926"
      )
  })
  @Operation(
      summary = "공지사항 상세 조회",
      description = """
      특정 공지사항의 상세 내용을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - noticePostId (필수): 조회할 공지사항 게시글 ID
      
      **응답 데이터**
      - NoticePostDto: 공지사항 상세 정보
        * noticePost: 공지사항 게시글 내용
        * title: 공지사항 제목
        * content: 공지사항 본문
        * viewCount: 조회수
        * createdAt: 작성일시
      
      **예외 상황**
      - NOTICE_POST_NOT_FOUND (404): 공지사항을 찾을 수 없음
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      
      **참고사항**
      - 조회 시 해당 공지사항의 조회수가 자동으로 1 증가
      - 삭제된 공지사항은 조회 불가
      """
  )
  ResponseEntity<NoticePostDto> getNoticePost(
      CustomUserDetails customUserDetails,
      NoticePostCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.10.01",
          author = Author.SUHSAECHAN,
          description = "공지사항 좋아요 init"
      )
  })
  @Operation(
      summary = "공지사항 좋아요",
      description = """
          **특정 공지사항 글 좋아요 증가**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 좋아요를 누른 공지사항 글 PK [필수]

          - **ContentType contentType**: 공지사항 [필수]
            _예: ContentType.NOTICE_

          **반환 파라미터 값:**

          - **NoticePostDto**: 공지사항 게시판 정보 반환
            - **NoticeBoardLike noticeBoardLike**: 좋아요 내역

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 공지사항에 좋아요를 누를 수 있습니다.
          - 본인이 작성한 글에는 좋아요를 누를 수 없습니다.
          - 이미 좋아요를 누른 글에는 중복으로 요청할 수 없습니다.
          """
  )
  ResponseEntity<NoticePostDto> noticePostLike(
      CustomUserDetails customUserDetails,
      NoticePostCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.10.01",
          author = Author.BAEKJIHOON,
          description = "공지사항 좋아요 취소 init"
      )
  })
  @Operation(
      summary = "공지사항 좋아요 취소",
      description = """
          **공지사항 좋아요 취소**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 공지사항 글 PK [필수]

          - **ContentType contentType**: 공지사항 [필수]
            _예: ContentType.NOTICE_

          **반환 파라미터 값:**
          `없음`

          **참고 사항:**

          - 이 API를 통해 사용자는 공지사항 좋아요 취소가 가능합니다.
          - 좋아요를 누른 적이 없는 글에는 취소 요청을 할 수 없습니다.
          """
  )
  ResponseEntity<Void> noticePostCancelLike(
      CustomUserDetails customUserDetails,
      NoticePostCommand command);
}
