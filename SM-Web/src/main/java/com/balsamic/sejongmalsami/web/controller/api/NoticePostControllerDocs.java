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
      summary = "공지사항 글 필터링 조회",
      description = """
      이 API는 인증이 필요하며, JWT 토큰이 필요합니다.

      #### 요청 파라미터
      - **`String query`**: 공지사항 제목 검색어

      #### 반환 파라미터
      - **`NoticePostDto`**: 공지사항 글 정보 반환
        - **`Page<NoticePost> noticePostsPage`**: 필터링 된 공지사항 글 리스트
      
      #### 참고사항
      - 검색어 입력 시 공지사항 제목에 해당 검색어가 포함 된 글을 반환합니다.
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
      summary = "PIN된 공지사항 글 조회",
      description = """
      이 API는 인증이 필요하며, JWT 토큰이 필요합니다.

      #### 요청 파라미터
      `없음`

      #### 반환 파라미터
      - **`NoticePostDto`**: 공지사항 글 정보 반환
        - **`List<NoticePost> noticePosts`**: PIN 된 공지사항 글 리스트
      
      #### 참고사항
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
      summary = "공지사항 단일 글 조회",
      description = """
      이 API는 인증이 필요하며, JWT 토큰이 필요합니다.

      #### 요청 파라미터
      - **`UUID noticePostId`**: 조회할 공지사항 글 ID

      #### 반환 파라미터
      - **`NoticePostDto`**: 공지사항 글 정보 반환
        - **`NoticePost noticePost`**: 조회된 공지사항 글 정보
      
      #### 참고사항
      - 조회 시 조회수가 자동으로 1 증가합니다.
      - 존재하지 않는 공지사항 ID로 조회 시 NOTICE_POST_NOT_FOUND 에러가 발생합니다.
      """
  )
  ResponseEntity<NoticePostDto> getNoticePost(
      CustomUserDetails customUserDetails,
      NoticePostCommand command
  );
}
