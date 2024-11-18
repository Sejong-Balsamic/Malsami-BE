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

          - 이 API를 통해 사용자는 특정 게시글에 댓글을 작성할 수 있습니다.
          - 제목, 본문은 null 값이 들어갈 수 없습니다.
          - 성공적인 등록 후, 등록 된 자료 요청 글을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> saveDocumentRequestPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

}
