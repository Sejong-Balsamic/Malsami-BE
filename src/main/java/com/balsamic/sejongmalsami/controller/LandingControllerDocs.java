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
          date = "2024.11.28",
          author = Author.BAEKJIHOON,
          description = "캐싱 로직 삭제"
      ),
      @ApiChangeLog(
          date = "2024.11.1",
          author = Author.BAEKJIHOON,
          description = "Page<QuestionPost> 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.31",
          author = Author.BAEKJIHOON,
          description = "인기 질문글 캐싱 로직 수정에 따른 입력 파라미터 수정"
      ),
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
          ### 일간 인기 질문글 요청
          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 됩니다.`

          #### 요청 파라미터
          - **`pageNumber`** (`Integer`, 선택): 페이지 번호 (기본값 = 0)
          
          - **`pageSize`** (`Integer`, 선택): 조회하고 싶은 일간 인기 질문 글 개수 (기본값 = 30)
  
          #### 반환 파라미터
          - **`QuestionDto`**: 질문 게시판 정보 반환
            - **`Page<QuestionPost> questionPosts`**: 일간 인기 질문 글 리스트

          ### 참고 사항
          - 이 API를 통해 사용자는 일간 인기 질문글을 조회할 수 있습니다.
          - 요청 시각으로부터 24시간 이내에 작성된 n개의 일간 인기 질문 글을 조회합니다.
          - pageSize 파라미터를 설정하지 않으면 기본값 30이 할당됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> getDailyPopularQuestionPost(
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.28",
          author = Author.BAEKJIHOON,
          description = "캐싱 로직 삭제"
      ),
      @ApiChangeLog(
          date = "2024.11.1",
          author = Author.BAEKJIHOON,
          description = "Page<QuestionPost> 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.31",
          author = Author.BAEKJIHOON,
          description = "인기 질문글 캐싱 로직 수정에 따른 입력 파라미터 수정"
      ),
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
        ### 주간 인기 질문글 요청
        이 API는 인증이 필요하지 않으며, JWT 토큰이 존재하지 않아도 됩니다.

        #### 요청 파라미터
        - **`pageNumber`** (`Integer`, 선택): 페이지 번호 (기본값 = 0)
        
        - **`pageSize`** (`Integer`, 선택): 조회하고 싶은 주간 인기 질문 글 개수 (기본값 = 30)

        #### 반환 파라미터
        - **`QuestionDto`**: 질문 게시판 정보 반환
          - **`Page<QuestionPost> questionPosts`**: 주간 인기 질문 글 리스트
          
        #### 참고 사항
        - 이 API를 통해 사용자는 주간 인기 질문글을 조회할 수 있습니다.
        - 요청 시각으로부터 7일 이내에 작성된 n개의 주간 인기 질문 글을 조회합니다.
        - pageSize 파라미터를 설정하지 않으면 기본값 30이 할당됩니다.
        - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
        """
  )
  ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost(
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.28",
          author = Author.BAEKJIHOON,
          description = "자료 글 일간 인기점수 24시간마다 초기화"
      ),
      @ApiChangeLog(
          date = "2024.11.15",
          author = Author.SUHSAECHAN,
          description = "자료 게시글 최근 5년간 글중에 dailyScore 큰 순으로 Pageable 반환으로 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 인기글 init"
      )
  })
  @Operation(
      summary = "일간 인기 자료글",
      description = """
          **자료 일간 인기글 요청**

          **이 API는 인증이 필요하지 않으며, JWT 토큰이 존재하지 않아도 됩니다.**

          **입력 파라미터 값:**

          #### 요청 파라미터
          - **`pageNumber`** (`Integer`, 선택): 페이지 번호 (기본값 = 0)
        
          - **`pageSize`** (`Integer`, 선택): 조회하고 싶은 주간 인기 질문 글 개수 (기본값 = 30)

          ####반환 파라미터 값
          - **DocumentDto**: 자료 게시판 정보 반환
            - **Page\\<DocumentPost\\> documentPostsPage**: 일간 자료 인기글 리스트

          ####참고 사항
          - 이 API를 통해 사용자는 일간 인기 자료글을 조회할 수 있습니다.
          - 자료 게시판 일간 인기 점수는 매일 자정마다 초기화됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> getDailyPopularDocumentPost(
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.28",
          author = Author.BAEKJIHOON,
          description = "자료 글 주간 인기점수 7일마다 초기화"
      ),
      @ApiChangeLog(
          date = "2024.11.15",
          author = Author.SUHSAECHAN,
          description = "json형식 버그 수정, 자료 게시글 최근 5년간 글중에 weeklyScore 큰 순으로 Pageable 반환으로 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 인기글 init"
      )
  })
  @Operation(
      summary = "주간 인기 자료글",
      description = """
          **자료 주간 인기글 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          #### 요청 파라미터
          - **`pageNumber`** (`Integer`, 선택): 페이지 번호 (기본값 = 0)
        
          - **`pageSize`** (`Integer`, 선택): 조회하고 싶은 주간 인기 질문 글 개수 (기본값 = 30)

          ####반환 파라미터 값
          - **DocumentDto**: 자료 게시판 정보 반환
            - **Page\\<DocumentPost\\> documentPostsPage**: 주간 자료 인기글 리스트

          ####참고 사항
          - 이 API를 통해 사용자는 주간 인기 자료글을 조회할 수 있습니다.
          - 자료 게시판 일간 인기 점수는 매주 월요일 자정마다 초기화됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> getWeeklyPopularDocumentPost(
      DocumentCommand command);
}
