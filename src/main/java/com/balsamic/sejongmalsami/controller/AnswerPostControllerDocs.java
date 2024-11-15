package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface AnswerPostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.14",
          author = Author.BAEKJIHOON,
          description = "질문게시판 dto, command 통합"
      ),
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

          - **UUID questionPostId**: 질문글 PK [필수]

          - **String content**: 답변 본문 [필수]
           \s
          - **List\\<MultipartFile\\> mediaFiles**: 첨부파일 [선택]
            - 최대 3개까지만 추가가능, 이미지파일만 업로드가능
                   \s
          - **Boolean isPrivate**: 내 정보 비공개 여부 (default = false) [선택]

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **AnswerPost answerPost**: 답변 글 정보
            - **List\\<MediaFile\\> mediaFiles**: 답변 글 첨부파일

          **참고 사항:**

          - 이 API를 통해 사용자는 질문 글에 답변을 동록할 수 있습니다.
          - 질문글 PK, 본문은 null 값이 들어갈 수 없습니다. [필수]
          - 첨부파일은 이미지 파일만 지원합니다.
          - 내 정보 비공개 여부는 프론트에서 설정하지 않으면 default 값이 할당됩니다.
          - 성공적인 등록 후, 등록 된 답변을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> saveAnswerPost(
      CustomUserDetails customUserDetails,
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.16",
          author = Author.SUHSAECHAN,
          description = "질문 게시글도 채택여부 업데이트 및 수정 저장"
      ),
      @ApiChangeLog(
          date = "2024.10.29",
          author = Author.BAEKJIHOON,
          description = "답변 채택"
      )
  })
  @Operation(
      summary = "답변 채택",
      description = """
          **답변 채택 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 답변 글 PK [필수]

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **AnswerPost answerPost**: 채택 된 답변 글 정보

          **참고 사항:**

          - 이 API를 통해 사용자는 등록된 답변을 채택할 수 있습니다.
          - 성공 후, 채택 된 답변을 반환합니다.
          - 질문 글 작성자와 답변 글 작성자가 같은 경우 채택 불가능합니다.
          - 질문 글 작성자만 답변 채택이 가능합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> chaetaekAnswerPost(
      CustomUserDetails customUserDetails,
      QuestionCommand command);
}
