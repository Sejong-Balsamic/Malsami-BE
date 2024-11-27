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
          date = "2024.11.27",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 전체 질문 글 조회"
      )
  })
  @Operation(
      summary = "질문 글 필터링 조회",
      description = """
        ### 질문 글 필터링 조회 요청
        이 API는 인증이 필요하지 않습니다.

        #### 요청 파라미터
        - **`subject`** (`String`, 선택): 교과목명 필터링
        - **`questionPresetTags`** (`List<QuestionPresetTag>`, 선택): 정적 태그 필터링 (최대 2개)
        - **`faculty`** (`Faculty`, 선택): 단과대별 필터링
        - **`chaetaekStatus`** (`ChaetaekStatus`, 선택): 채택 상태 필터링 (전체, 채택, 미채택)
        - **`sortType`** (`SortType`, 선택): 정렬 조건 (최신순, 좋아요순, 엽전 현상금순, 조회순)
        - **`pageNumber`** (`Integer`, 선택): 조회할 페이지 번호 (기본값 = 0)
        - **`pageSize`** (`Integer`, 선택): 한 페이지에 조회할 글 개수 (기본값 = 30)

        #### 반환 파라미터
        - **`Page<QuestionPost> questionPostsPage`**: 필터링된 질문 글 리스트

        #### 정적 태그
        *총 7개의 정적 태그가 존재하며, 최대 2개까지 설정 가능합니다.*
        - **OUT_OF_CLASS**: 수업 외 내용
        - **UNKNOWN_CONCEPT**: 개념 모름
        - **BETTER_SOLUTION**: 더 나은 풀이
        - **EXAM_PREPARATION**: 시험 대비
        - **DOCUMENT_REQUEST**: 자료 요청
        - **STUDY_TIPS**: 공부 팁
        - **ADVICE_REQUEST**: 조언 구함
        
        #### 정렬 타입
        - **LATEST**: 최신순
        - **MOST_LIKED**: 좋아요순
        - **YEOPJEON_REWARD**: 엽전 현상금 순
        - **VIEW_COUNT**: 조회수 순

        #### 채택 여부
        - **ALL**: 전체
        - **CHAETAEK**: 채택
        - **NO_CHAETAEK**: 미채택
        """
  )
  ResponseEntity<QuestionDto> getFilteredQuestionPosts(QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.27",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 전체 자료 글 조회"
      )
  })
  @Operation(
      summary = "자료글 필터링 조회",
      description = """
          **자료 글 필터링 조회 요청**

          **이 API는 인증이 필요하지 않습니다.**

          **입력 파라미터 값:**
          
          - **String subject**: 교과목명 필터링 [선택]
          
          - **List<DocumentType> documentTypes**: 태그 필터링 (최대 2개) [선택]
          
          - **Faculty faculty**: 단과대 필터링 [선택]
          
          - **PostTier postTier**: 자료 등급별 필터링 [선택]
          
          - **SortType sortType**: 정렬 기준 [선택] (default = 최신순)
          
          - **Integer pageNumber**: 조회하고싶은 페이지 번호 [선택] (default = 0)
           
          - **Integer pageSize**: 한 페이지에 조회하고싶은 글 개수 [선택] (default = 30)
          

          **반환 파라미터 값:**

          - **DocumentDto**: 자료 게시판 정보 반환
            - **Page\\<DocumentPost\\> documentPostsPage**: 필터링 된 자료글 리스트
          
          ### **DocumentType**
  
          최대 2개까지의 카테고리를 설정 가능
  
          - **DOCUMENT**: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
          - **PAST_EXAM**: 퀴즈, 기출 문제, 과제 등
          - **SOLUTION**: 솔루션 등
          
          **정렬 타입**
          
          - **LATEST** (최신순)
          - **MOST_LIKED** (좋아요순)
          - **VIEW_COUNT** (조회수 순)
   
          **참고 사항:**

          - 이 API를 통해 사용자는 자료 게시판에 작성된 글을 필터링하여 조회할 수 있습니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          - pageNumber = 3, pageSize = 10 입력시 4페이지에 해당하는 10개의 글을 반환합니다. (31번째 글 ~ 40번째 글 반환)
          """
  )
  ResponseEntity<DocumentDto> getFilteredDocumentPosts(DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.27",
          author = Author.BAEKJIHOON,
          description = "랜딩페이지 전체 자료 요청 글 조회"
      )
  })
  @Operation(
      summary = "자료 요청 글 필터링",
      description = """
          **자료 요청 글 필터링**

          **이 API는 인증이 필요하지 않습니다.**
          
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
  ResponseEntity<DocumentDto> getFilteredDocumentRequestPosts(DocumentCommand command);

  @ApiChangeLogs({
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
          **일간 인기 질문글 요청**

          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 됩니다.`

          **입력 파라미터 값:**
           
          - **Integer pageSize**: 조회하고 싶은 일간 인기 질문 글 개수 [선택] (default = 30)
          
            _예: 10_ (총 10개의 일간 인기 질문글이 반환됩니다.)

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **Page\\<QuestionPost\\> questionPosts**: 일간 인기 질문 글 리스트

          **참고 사항:**

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
          **주간 인기 질문글 요청**

          `이 API는 인증이 필요없으며, JWT 토큰이 존재하지 않아도 됩니다.`

          **입력 파라미터 값:**
           
          - **Integer pageSize**: 조회하고 싶은 주간 인기 질문 글 개수 [선택] (default = 30)
          
            _예: 10_ (총 10개의 주간 인기 질문글이 반환됩니다.)

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **Page\\<QuestionPost\\> questionPosts**: 주간 인기 질문 글 리스트

          **참고 사항:**

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
