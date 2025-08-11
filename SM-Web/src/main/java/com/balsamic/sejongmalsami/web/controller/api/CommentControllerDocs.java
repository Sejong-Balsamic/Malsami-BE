package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.post.dto.CommentCommand;
import com.balsamic.sejongmalsami.post.dto.CommentDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface CommentControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.09.30",
          author = Author.BAEKJIHOON,
          description = "댓글 작성 init"
      )
  })
  @Operation(
      summary = "댓글 등록",
      description = """
          **댓글 등록 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **String content**: 댓글 내용 [필수]

          - **String postId**: 댓글이 속한 게시글의 ID [필수]

          - **Enum ContentType**: 댓글이 속한 게시글의 유형 [필수]

          - **Boolean isPrivate**: 내 정보 비공개 여부 (default = false) [선택]

            _기본값은 false입니다. true로 요청할 시 댓글에 내 정보가 비공개 처리됩니다._

          **게시글 유형 Enum**

          총 4개의 게시글 유형 중 해당하는 게시글 유형을 선택합니다..
          - **QUESTION** (질문글)
          - **ANSWER** (답변글)
          - **DOCUMENT** (자료글)
          - **DOCUMENT_REQUEST** (자료요청글)

            _예: "formData.append('contentType', 'QUESTION');_

          **반환 파라미터 값:**

          - **CommentDto**: 작성 된 댓글 반환
            - **Comment comment**: 댓글 정보

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 게시글에 댓글을 작성할 수 있습니다.
          - 본문, 게시글ID, 게시글의 유형은 null 값이 들어갈 수 없습니다.
          - 성공적인 등록 후, 등록 된 댓글을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<CommentDto> saveComment(
      CustomUserDetails customUserDetails,
      CommentCommand command);


  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.11",
          author = Author.BAEKJIHOON,
          description = "특정 글 댓글 조회 init"
      )
  })
  @Operation(
      summary = "특정 글에 작성된 댓글 조회 (최신순)",
      description = """
          **특정 글에 작성된 모든 댓글 조회**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 특정 글 PK [필수]

          - **ContentType contentType**: 글 Type [필수]

          - **Integer pageNumber**: n번째 페이지 [선택] (default: 0)

          - **Integer pageSize**: n개의 데이터 [선택] (default: 30)

          **반환 파라미터 값:**

          - **CommentDto**: 댓글 정보 반환
            - **Page\\<Comment\\> commentsPage**: 특정 글에 작성된 댓글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 글 or 답변에 작성된 모든 댓글을 조회할 수 있습니다.
          - 댓글은 최신순으로 정렬 후 반환됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          - pageNumber = 3, pageSize = 10 입력시 4페이지에 해당하는 10개의 댓글을 반환합니다. (31번째 댓글 ~ 40번째 댓글 반환)
          """
  )
  ResponseEntity<CommentDto> getAllCommentsByPostId(
      CustomUserDetails customUserDetails,
      CommentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "LikeController -> CommentController API 이관"
      ),
      @ApiChangeLog(
          date = "2024.11.11",
          author = Author.BAEKJIHOON,
          description = "댓글 좋아요 init"
      )
  })
  @Operation(
      summary = "댓글 좋아요 / 싫어요",
      description = """
          **특정 댓글 좋아요 / 싫어요**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 특정 댓글 PK [필수]
          - **LikeType likeType**: 좋아요 / 싫어요 타입 [필수]
          
          **LikeType**
          - **LIKE**
          - **DISLIKE**

          **반환 파라미터 값:**

          - **CommentDto**: 댓글 정보 반환
            - **CommentLike commentLike**: 좋아요 내역

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 댓글에 좋아요 or 싫어요 를 누를 수 있습니다.
          - 본인이 작성한 댓글에는 요청할 수 없습니다.
          - 이미 좋아요 or 싫어요 를 누른 댓글에는 중복으로 요청할 수 없습니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<CommentDto> commentLike(
      CustomUserDetails customUserDetails,
      CommentCommand command);
}
