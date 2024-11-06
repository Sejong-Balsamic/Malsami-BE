package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface QuestionPostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.14",
          author = Author.BAEKJIHOON,
          description = "질문게시판 command, dto 통합"
      ),
      @ApiChangeLog(
          date = "2024.10.11",
          author = Author.BAEKJIHOON,
          description = "질문 글 첨부파일 추가"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "질문 글 커스텀태그 작성 & 인기글"
      ),
      @ApiChangeLog(
          date = "2024.09.25",
          author = Author.BAEKJIHOON,
          description = "질문 글 등록"
      )
  })
  @Operation(
      summary = "질문 글 등록",
      description = """
          **글 등록 요청**
                                                                           
          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**
           
          **입력 파라미터 값:**
           
          - **String title**: 질문게시글 제목 [필수]
           
          - **String content**: 질문게시글 본문 [필수]

          - **String subject**: 교과목 명 [필수]

          - **List\\<MultipartFile\\> mediaFiles**: 첨부파일 (최대 3개까지만 추가가능, 이미지파일만 업로드가능) [선택]

          - **Set\\<QuestionPresetTag\\> questionPresetTagSet**: 질문 게시글 정적태그 (최대 2개까지만 선택가능) [선택]

          - **Set\\<String\\> customTagSet**: 질문 게시글 커스텀태그 (최대 4개까지만 추가가능) [선택]

          - **Integer reward**: 엽전 현상금 (default = 0) [선택]

          - **Boolean isPrivate**: 내 정보 비공개 여부 (default = false) [선택]
           

          **정적 태그**
           
          *총 7개의 정적태그가 존재하며 최대 2개까지의 정적태그를 설정할 수 있습니다.*
          - **OUT_OF_CLASS** (수업 외 내용)
          - **UNKNOWN_CONCEPT** (개념 모름)
          - **BETTER_SOLUTION** (더 나은 풀이)
          - **EXAM_PREPARATION** (시험 대비)
          - **DOCUMENT_REQUEST** (자료 요청)
          - **STUDY_TIPS** (공부 팁)
          - **ADVICE_REQUEST** (조언 구함)
          
          _예: "formData.append('questionPresetTagSet', 'DOCUMENT_REQUEST');_
           
           
          **반환 파라미터 값:**
           
          - **QuestionDto**: 질문 게시판 정보 반환
            - **QuestionPost questionPost**: 질문 글 정보
            - **List\\<MediaFile\\> mediaFiles**: 질문 글 첨부파일
            - **Set\\<String\\> customTags**: 질문 글 커스텀태그
           
          **참고 사항:**
           
          - 이 API를 통해 사용자는 질문게시판에 질문 글을 동록할 수 있습니다.
          - 글 제목, 본문, 과목명은 null 값이 들어갈 수 없습니다. [필수]
          - 첨부파일은 이미지 파일만 지원합니다.
          - 정적태그, 엽전 현상금, 내 정보 비공개 여부는 프론트에서 설정하지 않으면 default 값이 할당됩니다.
          - 엽전 현상금 null 또는 음수 값 입력시 자동으로 0으로 설정됩니다.
          - 성공적인 등록 후, 등록 된 질문글을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> saveQuestionPost(
      CustomUserDetails customUserDetails,
      QuestionCommand questionCommand);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.21",
          author = Author.BAEKJIHOON,
          description = "특정 질문 글 조회"
      )
  })
  @Operation(
      summary = "특정 질문 글 조회",
      description = """
          **특정 질문 글 조회 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID postId**: 질문 글 PK [필수]
           \s
            _예: "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"_

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **QuestionPost questionPost**: 질문 글

          **참고 사항:**

          - 이 API를 통해 사용자는 postId 값에 해당하는 질문 글을 조회할 수 있습니다.
          - 반환된 질문글의 조회수가 1 증가합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> getQuestionPost(
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.1",
          author = Author.BAEKJIHOON,
          description = "pageable 설정"
      ),
      @ApiChangeLog(
          date = "2024.10.28",
          author = Author.BAEKJIHOON,
          description = "입력 파라미터 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.21",
          author = Author.BAEKJIHOON,
          description = "전체 질문 글 조회"
      )
  })
  @Operation(
      summary = "전체 질문 글 조회 (최신순)",
      description = """
          **전체 질문 글 조회 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **Integer pageNumber**: 조회하고싶은 페이지 번호 [선택] (default = 0)
           
          - **Integer pageSize**: 한 페이지에 조회하고싶은 글 개수 [선택] (default = 30)

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **Page\\<QuestionPost\\> questionPosts**: 전체 질문 글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 전체 질문 글을 조회할 수 있습니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          - pageNumber = 3, pageSize = 10 입력시 4페이지에 해당하는 10개의 글을 반환합니다. (31번째 글 ~ 40번째 글 반환)
          """
  )
  ResponseEntity<QuestionDto> getAllQuestionPost(
      QuestionCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.6",
          author = Author.BAEKJIHOON,
          description = "단과대 필터링 추가"
      ),
      @ApiChangeLog(
          date = "2024.11.1",
          author = Author.BAEKJIHOON,
          description = "답변 개수가 0개인 글 조회"
      )
  })
  @Operation(
      summary = "답변 개수가 0개인 글 조회 및 단과대 필터링 (최신순)",
      description = """
          **답변 개수가 0개인 질문 글 조회 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**
          
          - **Faculty faculty**: 단과대 필터링 [선택]

          - **Integer pageNumber**: 조회하고싶은 페이지 번호 [선택] (default = 0)
           
          - **Integer pageSize**: 한 페이지에 조회하고싶은 글 개수 [선택] (default = 30)

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **Page\\<QuestionPost\\> questionPosts**: 단과대 필터링이 적용 된 답변 개수가 0개인 질문글 리스트

          **참고 사항:**

          - 이 API를 통해 사용자는 아직 답변이 작성되지 않은 질문 글 최신순으로 조회할 수 있습니다.
          - 단과대 필터링 적용 시 해당 단과대가 적용된 글 중 답변 개수가 0개인 질문글을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          - pageNumber = 3, pageSize = 10 입력시 4페이지에 해당하는 10개의 글을 반환합니다. (31번째 글 ~ 40번째 글 반환)
          """
  )
  ResponseEntity<QuestionDto> getAllQuestionPostsNotAnswered(
      QuestionCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.6",
          author = Author.BAEKJIHOON,
          description = "파라미터 수정"
      ),
      @ApiChangeLog(
          date = "2024.11.4",
          author = Author.BAEKJIHOON,
          description = "질문 글 필터링 init"
      )
  })
  @Operation(
      summary = "질문 글 필터링 조회",
      description = """
          **질문 글 필터링 조회 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**
          
          - **String subject**: 교과목명 필터링 [선택]
          
          - **Integer minYeopjeon**: 엽전 현상금 최소 개수 [선택]
          
          - **Integer maxYeopjeon**: 엽전 현상금 최대 개수 [선택]
          
          - **Set<QuestionPresetTag> questionPresetTagSet**: 정적 태그 필터링 [선택]
          
          - **Faculty faculty**: 단과대별 필터링 [선택]
          
          - **Boolean viewNotChaetaek**: 아직 채택되지 않은 글 필터링 [선택] (default = false)
          
          - **SortType sortType**: 정렬 조건 [선택]
          
          - **Integer pageNumber**: 조회하고싶은 페이지 번호 [선택] (default = 0)
           
          - **Integer pageSize**: 한 페이지에 조회하고싶은 글 개수 [선택] (default = 30)
          

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **Page\\<QuestionPost\\> questionPosts**: 필터링 된 질문글 리스트
          
          **정적 태그**
           
          *총 7개의 정적태그가 존재하며 최대 2개까지의 정적태그를 설정할 수 있습니다.*
          - **OUT_OF_CLASS** (수업 외 내용)
          - **UNKNOWN_CONCEPT** (개념 모름)
          - **BETTER_SOLUTION** (더 나은 풀이)
          - **EXAM_PREPARATION** (시험 대비)
          - **DOCUMENT_REQUEST** (자료 요청)
          - **STUDY_TIPS** (공부 팁)
          - **ADVICE_REQUEST** (조언 구함)
          
          **정렬 타입**
          
          - **LATEST** (최신순)
          - **MOST_LIKED** (좋아요순)
          - **YEOPJEON_REWARD** (엽전 현상금 순)
          - **VIEW_COUNT** (조회수 순)

          **참고 사항:**

          - 이 API를 통해 사용자는 질문 게시판에 작성된 글을 필터링하여 조회할 수 있습니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          - pageNumber = 3, pageSize = 10 입력시 4페이지에 해당하는 10개의 글을 반환합니다. (31번째 글 ~ 40번째 글 반환)
          """
  )
  ResponseEntity<QuestionDto> getFilteredQuestionPosts(
      QuestionCommand command
  );

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
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "질문게시판 일간 인기글 init"
      )
  })
  @Operation(
      summary = "일간 인기 질문글",
      description = """
          **일간 인기 질문글 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

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
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "질문게시판 주간 인기글 init"
      )
  })
  @Operation(
      summary = "주간 인기 질문글",
      description = """
          **주간 인기 질문글 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

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
}
