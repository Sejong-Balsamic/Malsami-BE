package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.post.dto.QuestionCommand;
import com.balsamic.sejongmalsami.post.dto.QuestionDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface QuestionPostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.21",
          author = Author.SUHSAECHAN,
          description = "QuestionPost 첨부파일 로직 개선, 아직 Answer는 적용안한상태"
      ),
      @ApiChangeLog(
          date = "2024.11.15",
          author = Author.SUHSAECHAN,
          description = "(임시) ContentType 고려안함. QuestionPost의 comment 수 증가"
      ),
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
          
          #### 요청 파라미터
          - **`title`** (`String`, **필수**): 질문 게시글 제목
          - **`content`** (`String`, **필수**): 질문 게시글 본문
          - **`subject`** (`String`, **필수**): 교과목 명
          - **`attachmentFiles`** (`List<MultipartFile>`, 선택): 첨부파일 (최대 3개, 이미지 파일만 지원)
          - **`questionPresetTags`** (`List<QuestionPresetTag>`, 선택): 정적 태그 (최대 2개 선택 가능)
          - **`customTags`** (`List<String>`, 선택): 커스텀 태그 (최대 4개 추가 가능)
          - **`reward`** (`Integer`, 선택): 엽전 현상금 (기본값 = 0)
          - **`isPrivate`** (`Boolean`, 선택): 내 정보 비공개 여부 (기본값 = false)
          
          #### 반환 파라미터
          - **`QuestionPost questionPost`**: 질문 글 정보
          - **`List<MediaFile> mediaFiles`**: 첨부파일 리스트
          - **`Set<String> customTags`**: 커스텀 태그 리스트 
          
          
          **정적 태그**
          
          *총 7개의 정적태그가 존재하며 최대 2개까지의 정적태그를 설정할 수 있습니다.*
          - **OUT_OF_CLASS** (수업 외 내용)
          - **UNKNOWN_CONCEPT** (개념 모름)
          - **BETTER_SOLUTION** (더 나은 풀이)
          - **EXAM_PREPARATION** (시험 대비)
          - **DOCUMENT_REQUEST** (자료 요청)
          - **STUDY_TIPS** (공부 팁)
          - **ADVICE_REQUEST** (조언 구함)
          
          **참고 사항:**
          - 첨부파일은 이미지 파일만 지원합니다.
          """
  )
  ResponseEntity<QuestionDto> saveQuestionPost(
      CustomUserDetails customUserDetails,
      QuestionCommand questionCommand);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.27",
          author = Author.SUHSAECHAN,
          description = "반환값에 mediaFiles 추가"
      ),
      @ApiChangeLog(
          date = "2024.11.16",
          author = Author.SUHSAECHAN,
          description = "게시글 조회시 isChaetaek"
      ),
      @ApiChangeLog(
          date = "2024.11.15",
          author = Author.SUHSAECHAN,
          description = "답변 반환 로직 추가, 가짜 CUSTOM TAG 반환 로직 추가"
      ),
      @ApiChangeLog(
          date = "2024.10.21",
          author = Author.BAEKJIHOON,
          description = "특정 질문 글 조회"
      )
  })
  @Operation(
      summary = "특정 질문 글 조회",
      description = """
          ### 특정 질문 글 조회 요청
          이 API는 인증이 필요하며, JWT 토큰이 필요합니다.
          
          #### 요청 파라미터
          - **`postId`** (`UUID`, **필수**): 질문 글의 고유 식별자
          
          #### 반환 파라미터
          - **`QuestionPost questionPost`**: 질문 글 정보
          - **`List<String> customTags`**: 질문글의 태그 리스트
          - **`List<AnswerPost> answerPosts`**: 답변 리스트
          - **`List<MediaFiles> mediaFiles`**: 미디어 리스트
          """
  )
  ResponseEntity<QuestionDto> getQuestionPost(
      CustomUserDetails customUserDetails,
      QuestionCommand command);

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
          ### 답변 개수가 0개인 질문 글 조회 요청
          이 API는 인증이 필요하며, JWT 토큰이 필요합니다.
          
          #### 요청 파라미터
          - **`faculty`** (`Faculty`, 선택): 단과대 필터링
          - **`pageNumber`** (`Integer`, 선택): 조회할 페이지 번호 (기본값 = 0)
          - **`pageSize`** (`Integer`, 선택): 한 페이지에 조회할 글 개수 (기본값 = 30)
          
          #### 반환 파라미터
          - **`QuestionDto`**: 질문 게시판 정보 반환
            - **`Page<QuestionPost> questionPosts`**: 답변이 없는 글 리스트 (단과대 필터링 적용)
          """
  )
  ResponseEntity<QuestionDto> getAllQuestionPostsNotAnswered(
      QuestionCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.04.24",
          author = Author.BAEKJIHOON,
          description = "엽전 현상금 존재 최신순 정렬 추가"
      ),
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "API 인증 생략"
      ),
      @ApiChangeLog(
          date = "2024.12.6",
          author = Author.BAEKJIHOON,
          description = "엽전 현상금 순 정렬 시 자동으로 미채택 된 글만 조회"
      ),
      @ApiChangeLog(
          date = "2024.11.6",
          author = Author.SUHSAECHAN,
          description = "필터링 로직 수정, 현상금 범위 삭제, 채택여부 로직 변경"
      ),
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
          ### 질문 글 필터링 조회 요청
          **이 API는 인증이 필요하지 않습니다.**
          
          #### 요청 파라미터
          - **`query`** (`String`, 선택): 제목 + 본문 검색
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
          - **REWARD_YEOPJEON_DESCENDING**: 엽전 현상금 순
          - **REWARD_YEOPJEON_LATEST**: 엽전 현상금이 존재하는 글 최신순
          - **VIEW_COUNT**: 조회수 순
          
          #### 채택 여부
          - **ALL**: 전체
          - **CHAETAEK**: 채택
          - **NO_CHAETAEK**: 미채택
          
          ### 특이사항
          - 엽전 현상금 관련 정렬 (YEOPJEON_REWARD, YEOPJEON_REWARD_LATEST) 요청시 `미채택`된 글만 반환합니다
          - 엽전 현상금 관련 정렬 (YEOPJEON_REWARD, YEOPJEON_REWARD_LATEST) 요청시 엽전 현상금이 0으로 설정된 글은 반환하지 않습니다
          """
  )
  ResponseEntity<QuestionDto> getFilteredQuestionPosts(
      QuestionCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "API 인증 생략"
      ),
      @ApiChangeLog(
          date = "2024.12.11",
          author = Author.BAEKJIHOON,
          description = "Redis 사용"
      ),
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
          ### 일간 인기 질문글 요청
          이 API는 인증이 필요하지 않습니다.
          
          #### 요청 파라미터
          `없음`
          
          #### 반환 파라미터
          - **`QuestionDto`**: 질문 게시판 정보 반환
            - **`Page<QuestionPost> questionPosts`**: 일간 인기 질문 글 리스트
          
          #### 참고 사항
          - 이 API를 통해 사용자는 일간 인기 질문글을 조회할 수 있습니다.
          - 요청 시각으로부터 24시간 이내에 작성된 n개의 일간 인기 질문 글을 조회합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> getDailyPopularQuestionPost();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "API 인증 생략"
      ),
      @ApiChangeLog(
          date = "2024.12.11",
          author = Author.BAEKJIHOON,
          description = "Redis 사용"
      ),
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
          ### 주간 인기 질문글 요청
          이 API는 인증이 필요하지 않습니다
          
          #### 요청 파라미터
          `없음`
          
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
  ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.03.21",
          author = Author.BAEKJIHOON,
          description = "LikeController -> QuestionController API 이관"
      ),
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
            - **QuestionBoardLike questionBoardLike**: 좋아요 내역
          
          **참고 사항:**
          
          - 이 API를 통해 사용자는 특정 질문글 or 답변에 좋아요를 누를 수 있습니다.
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
          date = "2025.09.12",
          author = Author.BAEKJIHOON,
          description = "질문 게시판 좋아요 취소"
      )
  })
  @Operation(
      summary = "질문게시판 좋아요 취소",
      description = """
          **답변 좋아요 취소**
          
          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**
          
          **입력 파라미터 값:**
          
          - **UUID postId**: 답변 PK [필수]
          
          - **ContentType contentType**: 답변 [필수]
            _예: ContentType.ANSWER_
          
          **반환 파라미터 값:**
          `없음`
          
          **참고 사항:**
          
          - 이 API를 통해 사용자는 답변 좋아요 취소가 가능합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<Void> questionBoardCancelLike(
      CustomUserDetails customUserDetails,
      QuestionCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "AnswerController -> QuestionController API 이관"
      ),
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
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "AnswerController -> QuestionController API 이관"
      ),
      @ApiChangeLog(
          date = "2024.11.19",
          author = Author.BAEKJIHOON,
          description = "특정 질문 글에 작성 된 모든 답변 조회"
      )
  })
  @Operation(
      summary = "특정 질문 글에 작성 된 모든 답변 조회",
      description = """
          **작성 된 답변 조회**
          
          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**
          
          **입력 파라미터 값:**
          
          - **UUID questionPostId**: 질문 글 PK [필수]
          
          **반환 파라미터 값:**
          
          - **QuestionDto**: 질문 게시판 정보 반환
            - **List\\<AnswerPost\\> answerPosts**: 특정 질문 글에 작성된 답변 List
          
          **참고 사항:**
          
          - 이 API를 통해 사용자는 질문 글에 작성된 모든 답변을 조회할 수 있습니다.
          - 질문글에 작성된 답변이 존재하지 않으면 null 값을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> getAnswersByQuestion(
      CustomUserDetails customUserDetails,
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "AnswerController -> QuestionController API 이관"
      ),
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
