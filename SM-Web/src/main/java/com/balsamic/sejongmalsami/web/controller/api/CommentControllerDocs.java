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
      특정 게시글에 댓글을 작성합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - content (필수): 댓글 내용
      - postId (필수): 댓글이 속한 게시글의 ID
      - contentType (필수): 댓글이 속한 게시글의 유형
        * QUESTION: 질문글
        * ANSWER: 답변글
        * DOCUMENT: 자료글
        * DOCUMENT_REQUEST: 자료요청글
      - isPrivate (선택): 내 정보 비공개 여부 (기본값: false)
      
      **응답 데이터**
      - CommentDto: 작성된 댓글 정보
        * comment: 댓글 세부 정보
      
      **예외 상황**
      - COMMENT_CONTENT_REQUIRED (400): 댓글 내용이 필요함
      - POST_NOT_FOUND (404): 게시글을 찾을 수 없음
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      - 댓글 작성 시 작성자의 엽전이 증가함
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
      summary = "특정 글 댓글 조회",
      description = """
      특정 게시글에 작성된 모든 댓글을 최신순으로 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - postId (필수): 조회할 게시글의 ID
      - contentType (필수): 게시글 유형
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지 크기 (기본값: 30)
      
      **응답 데이터**
      - CommentDto: 댓글 목록 정보
        * commentsPage: 페이징된 댓글 리스트
      
      **예외 상황**
      - POST_NOT_FOUND (404): 게시글을 찾을 수 없음
      - UNAUTHORIZED (401): 인증이 필요함
      - INVALID_PAGE_REQUEST (400): 잘못된 페이지 요청
      
      **참고사항**
      - 댓글은 최신순으로 정렬됨
      - 페이지 번호는 0부터 시작
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
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
      summary = "댓글 좋아요/싫어요",
      description = """
      특정 댓글에 좋아요 또는 싫어요를 추가합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - postId (필수): 댓글 ID
      - contentType (필수): 게시글 유형
      - likeType (필수): 좋아요/싫어요 타입
        * LIKE: 좋아요
        * DISLIKE: 싫어요
      
      **응답 데이터**
      - CommentDto: 댓글 정보
        * commentLike: 좋아요/싫어요 내역
      
      **예외 상황**
      - COMMENT_NOT_FOUND (404): 댓글을 찾을 수 없음
      - SELF_LIKE_NOT_ALLOWED (400): 본인 댓글에는 좋아요/싫어요 불가
      - DUPLICATE_LIKE (400): 이미 좋아요/싫어요를 누른 댓글
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - 본인이 작성한 댓글에는 좋아요/싫어요 불가
      - 중복 좋아요/싫어요 방지
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      """
  )
  ResponseEntity<CommentDto> commentLike(
      CustomUserDetails customUserDetails,
      CommentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.09.12",
          author = Author.BAEKJIHOON,
          description = "댓글 좋아요/싫어요 취소"
      )
  })
  @Operation(
      summary = "댓글 좋아요/싫어요 취소",
      description = """
      특정 댓글에 누른 좋아요 또는 싫어요를 취소합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - postId (필수): 댓글 ID
      - contentType (필수): 게시글 유형
      - likeType (필수): 취소할 좋아요/싫어요 타입
        * LIKE: 좋아요 취소
        * DISLIKE: 싫어요 취소
      
      **응답 데이터**
      - 없음 (200 OK)
      
      **예외 상황**
      - COMMENT_NOT_FOUND (404): 댓글을 찾을 수 없음
      - LIKE_NOT_FOUND (404): 취소할 좋아요/싫어요가 없음
      - SELF_LIKE_NOT_ALLOWED (400): 본인 댓글에는 좋아요/싫어요 불가
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - 본인이 작성한 댓글에는 좋아요/싫어요 취소 불가
      - 기존에 누른 좋아요/싫어요가 있어야 취소 가능
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      """
  )
  ResponseEntity<Void> cancelCommentList(
      CustomUserDetails customUserDetails,
      CommentCommand command
  );
}
