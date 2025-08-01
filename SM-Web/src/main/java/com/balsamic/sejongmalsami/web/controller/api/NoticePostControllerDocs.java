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
}
