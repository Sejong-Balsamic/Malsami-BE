package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.Author;
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
      summary = "자료 요청 글 작성",
      description = """
          **자료 요청 글 작성**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **String title**: 글 제목 [필수]

          - **String content**: 글 본문 [필수]

          - **String subject**: 교과목명 [선택]

          - **List\\<DocumentType\\> documentTypes**: 자료 타입 (최대2개) [선택]

          - **Boolean isPrivate**: 내 정보 비공개 (default: false) [선택]
            _기본값은 false입니다. true로 요청할 시 댓글에 내 정보가 비공개 처리됩니다._

          ### **documentTypes**
  
          최대 2개까지의 카테고리를 설정 가능
  
          - **DOCUMENT**: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
          - **PAST_EXAM**: 퀴즈, 기출 문제, 과제 등
          - **SOLUTION**: 솔루션 등

            _예: "formData.append('documentTypes', 'SOLUTION');_

          **반환 파라미터 값:**

          - **DocumentDto**: 작성 된 자료 요청 글 정보
            - **DocumentRequestPost documentRequestPost**: 자료 요청 글 정보

          **참고 사항:**

          - 이 API를 통해 사용자는 자료 요청 글을 작성할 수 있습니다.
          - 자료요청글 페이지 접근 및 글 작성은 `중인 (엽전 1000냥)` 이상 가능합니다.
          - 제목, 본문은 null 값이 들어갈 수 없습니다.
          - 성공적인 등록 후, 등록 된 자료 요청 글을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> saveDocumentRequestPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.19",
          author = Author.BAEKJIHOON,
          description = "자료요청 글 필터링 조회 init"
      )
  })
  @Operation(
      summary = "자료 요청 글 필터링",
      description = """
          **자료 요청 글 필터링**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**
          
          **모든 필터링 미 선택 시 전체 글이 최신순으로 조회됩니다.**

          **입력 파라미터 값:**

          - **String subject**: 과목명 필터링 [선택]

          - **Faculty faculty**: 단과대 필터링 [선택]

          - **List\\<DocumentType\\> documentTypes**: 카테고리 필터링 (최대 2개) [선택]
          
          - **Integer pageNumber**: 페이지 번호 (default: 0) [선택]
          
          - **Integer pageSize**: 한 페이지에 조회할 글 개수 (default: 30) [선택]

          ### **documentTypes**
  
          최대 2개까지의 카테고리 필터링 적용 가능
  
          - **DOCUMENT**: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
          - **PAST_EXAM**: 퀴즈, 기출 문제, 과제 등
          - **SOLUTION**: 솔루션 등

            _예: "formData.append('documentTypes', 'SOLUTION');_

          **반환 파라미터 값:**

          - **DocumentDto**: 작성 된 자료 요청 글 정보
            - **Page\\<DocumentRequestPost\\> documentRequestPostsPage**: 자료 요청 글 page

          **참고 사항:**

          - 이 API를 통해 사용자는 자료 요청 게시판에 작성된 글을 필터링하여 조회할 수 있습니다.
          - 성공적인 등록 후, 필터링 된 자료 요청 글을 Page 객체로 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> getFilteredDocumentRequestPosts(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

}
