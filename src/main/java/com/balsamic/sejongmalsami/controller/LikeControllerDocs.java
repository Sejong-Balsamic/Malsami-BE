package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface LikeControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.28",
          author = Author.BAEKJIHOON,
          description = "질문 게시판 좋아요 init"
      )
  })
  @Operation(
      summary = "질문게시판 좋아요",
      description = """
          **특정 질문 글 or 답변 좋아요 증가**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 좋아요를 누른 질문 글 or 답변 PK [필수]
          
          - **ContentType contentType**: 질문 글 or 답변 글 [필수]
            _예: ContentType.QUESTION_

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **QuestionPost questionPost**: 특정 글 or 답변 정보 반환

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 글 or 답변에 좋아요를 누를 수 있습니다.
          - 본인이 작성한 글 or 답변에는 좋아요를 누를 수 없습니다.
          - 이미 좋아요를 누른 글 or 답변에는 중복으로 요청할 수 없습니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> questionBoardLike(
      CustomUserDetails customUserDetails,
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.11",
          author = Author.BAEKJIHOON,
          description = "댓글 좋아요 init"
      )
  })
  @Operation(
      summary = "댓글 좋아요",
      description = """
          **특정 댓글 좋아요**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 특정 댓글 PK [필수]

          **반환 파라미터 값:**

          - **CommentDto**: 댓글 정보 반환
            - **CommentLike commentLike**: 좋아요 내역

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 댓글에 좋아요를 누를 수 있습니다.
          - 본인이 작성한 댓글에는 좋아요를 누를 수 없습니다.
          - 이미 좋아요를 누른 댓글에는 중복으로 요청할 수 없습니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<CommentDto> commentLike(
      CustomUserDetails customUserDetails,
      CommentCommand command);
}
