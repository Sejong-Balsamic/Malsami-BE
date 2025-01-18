package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.QueryCommand;
import com.balsamic.sejongmalsami.object.QueryDto;
import com.balsamic.sejongmalsami.object.constants.Author;
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
      summary = "검색 페이지 init",
      description = """
          **검색 페이지**
                                                                           
          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**
           
          #### 요청 파라미터
          - **`query`** (`String`, 선택): 검색어
          - **`subject`** (`String`, 선택): 교과목 명
          - **`sortType`** (`SortType`, 선택): 정렬기준 (기본값 = 최신순)
          - **`pageNumber`** (`Integer`, 선택): 조회할 페이지 (기본값 = 0)
          - **`pageSize`** (`Integer`, 선택): 페이지 당 조회할 개수 (기본값 = 30)
          
          #### 반환 파라미터
          - **`Page<QuestionPost> questionPostsPage`**: 질문 글 리스트
          - **`Page<DocumentPost> documentPostsPage`**: 자료 글 리스트
          - **`Page<DocumentRequestPost> documentRequestPostsPage`**: 자료 요청 글 리스트
          - **`Page<NoticePost> noticePostsPage`**: 공지사항 글 리스트
          
          **참고 사항:**
          - 정렬기준은 `최신순`, `좋아요순`, `댓글순`, `조회순`, `과거순` 선택 가능합니다.
          - 검색어 입력 시 질문, 자료, 자료 요청, 공지사항 글 제목+본문에 해당 검색어가 포함된 글을 조회합니다.
          - 질문, 자료, 자료 요청, 공지사항 글은 각각 pageSize 개수만큼 조회됩니다.
            _예: pageSize = 30, 질문글 - 30개, 자료글 - 30개, 자료요청글 - 30개, 공지사항글 - 30개
          """
  )
  ResponseEntity<QueryDto> getPostsByQuery(
      QueryCommand command);
}
