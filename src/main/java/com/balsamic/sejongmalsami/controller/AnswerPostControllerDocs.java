package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.AnswerPostCommand;
import com.balsamic.sejongmalsami.object.AnswerPostDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface AnswerPostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.11",
          author = Author.BAEKJIHOON,
          description = "답변 글 등록"
      )
  })
  @Operation(
      summary = "답변 글 등록",
      description = """
          **글 등록 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID questionPostId**: 질문글 PK (required)
            _예: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"_

          - **String content**: 답변 본문 (required)
            _예: "이 문제 포인터 활용해야하는 문제에요. 포인터 먼저 공부해보세요~"_
            
          - **List\\<MultipartFile\\> mediaFiles**: 첨부파일 (최대 3개까지만 추가가능, 이미지파일만 업로드가능)
          
          - **Boolean isChaetaek**: 채택 여부 (default = false)
            -기본값은 false입니다.
          
          - **Boolean isPrivate**: 내 정보 비공개 여부 (default = false)
            _기본값은 false입니다. true로 요청할 시 질문 글에 내 정보가 비공개 처리됩니다._

          **반환 파라미터 값:**

          - **AnswerPostDto**: 작성 된 답변 반환
            - **AnswerPost answerPost**: 답변 정보

          **참고 사항:**

          - 이 API를 통해 사용자는 질문 글에 답변을 동록할 수 있습니다.
          - 질문글 PK, 본문은 null 값이 들어갈 수 없습니다. (required)
          - 첨부파일은 이미지 파일만 지원합니다.
          - 채택 여부, 내 정보 비공개 여부는 프론트에서 설정하지 않으면 default 값이 할당됩니다.
          - 성공적인 등록 후, 등록 된 답변을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<AnswerPostDto> saveAnswerPost(
      CustomUserDetails customUserDetails,
      AnswerPostCommand command);
}
