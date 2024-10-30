package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface LandingControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.30",
          author = Author.BAEKJIHOON,
          description = "pageable 입력 파라미터 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.23",
          author = Author.BAEKJIHOON,
          description = "pageable 추가, 상위 n개의 인기글 조회"
      ),
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 인기글 init"
      )
  })
  @Operation(
      summary = "일간 인기 질문글",
      description = """
          **일간 인기 질문글 요청**

          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 무방합니다.`

          **입력 파라미터 값:**

          - **Integer pageNum**: 조회하고싶은 질문 글 페이지 [선택]
          
            _예: 0_ (첫번째 페이지를 반환합니다) default = 0
           
          - **Integer pageSize**: 한 페이지에 조회하고싶은 질문 글 개수 [선택]
          
            _예: 30_ (총 30개의 일간 인기 질문글이 반환됩니다.) default = 30

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **List\\<QuestionPost\\> questionPosts**: 일간 인기 질문 글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 일간 인기 질문글을 조회할 수 있습니다.
          - 요청 시각으로부터 24시간 이내에 작성된 pageNum 번째 페이지의 pageSize 개의 일간 인기글을 조회합니다.
          - pageNum, pageSize 파라미터를 설정하지 않으면 기본값이 할당됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> getDailyPopularQuestionPost(
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.30",
          author = Author.BAEKJIHOON,
          description = "pageable 입력 파라미터 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.23",
          author = Author.BAEKJIHOON,
          description = "pageable 추가, 상위 n개의 인기글 조회"
      ),
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 인기글 init"
      )
  })
  @Operation(
      summary = "주간 인기 질문글",
      description = """
          **주간 인기 질문글 요청**

          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 무방합니다.`

          **입력 파라미터 값:**

          - **Integer pageNum**: 조회하고싶은 질문 글 페이지 [선택]
          
            _예: 0_ (첫번째 페이지를 반환합니다) default = 0
           
          - **Integer pageSize**: 한 페이지에 조회하고싶은 질문 글 개수 [선택]
          
            _예: 30_ (총 30개의 일간 인기 질문글이 반환됩니다.) default = 30

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **List\\<QuestionPost\\> questionPosts**: 주간 인기 질문 글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 주간 인기 질문글을 조회할 수 있습니다.
          - 요청 시각으로부터 7일 이내에 작성된 pageNum 번째 페이지의 pageSize 개의 주간 인기글을 조회합니다.
          - pageNum, pageSize 파라미터를 설정하지 않으면 기본값이 할당됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost(
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 인기글 init"
      )
  })
  @Operation(
      summary = "일간 인기 자료글",
      description = """
          **일간 인기 자료글 요청**

          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 무방합니다.`

          **입력 파라미터 값:**

          `없음`

          **반환 파라미터 값:**

          - **DocumentDto**: 자료 게시판 정보 반환
            - **List\\<DocumentPost\\> documentPosts**: 일간 자료 인기글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 일간 인기 자료글을 조회할 수 있습니다.
          - 요청 시각으로부터 24시간 이내에 작성된 상위 30개의 일간 인기글을 조회합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> getDailyPopularDocumentPost(
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 인기글 init"
      )
  })
  @Operation(
      summary = "주간 인기 자료글",
      description = """
          **주간 인기 자료글 요청**

          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 무방합니다.`

          **입력 파라미터 값:**

          `없음`

          **반환 파라미터 값:**

          - **DocumentDto**: 자료 게시판 정보 반환
            - **List\\<DocumentPost\\> documentPosts**: 주간 자료 인기글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 주간 인기 자료글을 조회할 수 있습니다.
          - 요청 시각으로부터 7일 이내에 작성된 상위 30개의 주간 인기글을 조회합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> getWeeklyPopularDocumentPost(
      DocumentCommand command);
}
