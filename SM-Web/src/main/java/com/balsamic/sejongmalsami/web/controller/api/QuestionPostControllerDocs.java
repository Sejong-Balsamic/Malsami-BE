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
      summary = "질문글 등록",
      description = """
      새로운 질문글을 등록합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - title (필수): 질문 게시글 제목
      - content (필수): 질문 게시글 본문
      - subject (필수): 교과목명
      - attachmentFiles (선택): 첨부파일 목록 (최대 3개, 이미지만 지원)
      - questionPresetTags (선택): 정적 태그 목록 (최대 2개)
        * OUT_OF_CLASS: 수업 외 내용
        * UNKNOWN_CONCEPT: 개념 모름
        * BETTER_SOLUTION: 더 나은 풀이
        * EXAM_PREPARATION: 시험 대비
        * DOCUMENT_REQUEST: 자료 요청
        * STUDY_TIPS: 공부 팁
        * ADVICE_REQUEST: 조언 구함
      - customTags (선택): 커스텀 태그 목록 (최대 4개)
      - reward (선택): 엽전 현상금 (기본값: 0)
      - isPrivate (선택): 내 정보 비공개 여부 (기본값: false)
      
      **응답 데이터**
      - QuestionDto: 등록된 질문글 정보
        * questionPost: 질문글 정보
        * mediaFiles: 첨부파일 목록
        * customTags: 커스텀 태그 목록
      
      **예외 상황**
      - INVALID_REQUEST (400): 필수 파라미터 누락 또는 유효하지 않은 데이터
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - FILE_UPLOAD_ERROR (400): 첨부파일 업로드 실패
      
      **참고사항**
      - 첨부파일은 이미지 파일만 지원
      - 정적 태그와 커스텀 태그는 조합하여 최대 6개까지 설정 가능
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
      summary = "특정 질문글 조회",
      description = """
      질문글 ID로 특정 질문글의 상세 정보를 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - postId (필수): 질문글 고유 식별자 (UUID)
      
      **응답 데이터**
      - QuestionDto: 질문글 상세 정보
        * questionPost: 질문글 정보
        * customTags: 커스텀 태그 목록
        * answerPosts: 답변 목록
        * mediaFiles: 첨부파일 목록
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - NOT_FOUND (404): 존재하지 않는 질문글 ID
      - FORBIDDEN (403): 비공개 게시글 접근 권한 없음
      
      **참고사항**
      - 조회 시 조회수가 자동으로 증가
      - 채택된 답변이 있는 경우 우선 표시
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
      summary = "미답변 질문글 조회",
      description = """
      답변이 없는 질문글을 최신순으로 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - faculty (선택): 단과대 필터링
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지당 항목 수 (기본값: 30)
      
      **응답 데이터**
      - QuestionDto: 질문글 목록 정보
        * questionPosts: 미답변 질문글 페이지 정보
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - INVALID_PARAMETER (400): 잘못된 페이지 파라미터
      
      **참고사항**
      - 답변 개수가 0개인 질문글만 조회
      - 단과대 필터링 적용 가능
      - 최신 등록순으로 정렬
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
      summary = "질문글 필터링 조회",
      description = """
      다양한 조건으로 질문글을 필터링하여 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - query (선택): 제목 및 본문 검색어
      - subject (선택): 교과목명 필터링
      - questionPresetTags (선택): 정적 태그 필터링 (최대 2개)
        * OUT_OF_CLASS: 수업 외 내용
        * UNKNOWN_CONCEPT: 개념 모름
        * BETTER_SOLUTION: 더 나은 풀이
        * EXAM_PREPARATION: 시험 대비
        * DOCUMENT_REQUEST: 자료 요청
        * STUDY_TIPS: 공부 팁
        * ADVICE_REQUEST: 조언 구함
      - faculty (선택): 단과대별 필터링
      - chaetaekStatus (선택): 채택 상태 필터링
        * ALL: 전체
        * CHAETAEK: 채택
        * NO_CHAETAEK: 미채택
      - sortType (선택): 정렬 조건
        * LATEST: 최신순
        * MOST_LIKED: 좋아요순
        * REWARD_YEOPJEON_DESCENDING: 엽전 현상금순
        * REWARD_YEOPJEON_LATEST: 엽전 현상금 글 최신순
        * VIEW_COUNT: 조회수순
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지당 항목 수 (기본값: 30)
      
      **응답 데이터**
      - QuestionDto: 필터링된 질문글 목록
        * questionPostsPage: 페이지 정보가 포함된 질문글 목록
      
      **예외 상황**
      - INVALID_PARAMETER (400): 잘못된 필터링 파라미터
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 엽전 현상금 관련 정렬 시 미채택 글만 반환
      - 엽전 현상금이 0인 글은 현상금 정렬에서 제외
      - 정적 태그는 최대 2개까지 선택 가능
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
      summary = "일간 인기 질문글 조회",
      description = """
      최근 24시간 내 인기 질문글을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - QuestionDto: 일간 인기 질문글 정보
        * questionPosts: 일간 인기 질문글 페이지 정보
      
      **예외 상황**
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      - CACHE_ERROR (500): 캐시 조회 실패
      
      **참고사항**
      - 24시간 이내 작성된 질문글 중 좋아요 및 조회수 기반 인기도 산정
      - Redis 캐시를 통한 빠른 응답 제공
      - 실시간 업데이트되는 인기도 순위
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
      summary = "주간 인기 질문글 조회",
      description = """
      최근 7일 내 인기 질문글을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - QuestionDto: 주간 인기 질문글 정보
        * questionPosts: 주간 인기 질문글 페이지 정보
      
      **예외 상황**
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      - CACHE_ERROR (500): 캐시 조회 실패
      
      **참고사항**
      - 7일 이내 작성된 질문글 중 좋아요 및 조회수 기반 인기도 산정
      - Redis 캐시를 통한 빠른 응답 제공
      - 기본 페이지 크기는 30개
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
      질문글 또는 답변에 좋아요를 추가합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - postId (필수): 좋아요할 게시글 또는 답변의 ID (UUID)
      - contentType (필수): 컨텐츠 타입
        * QUESTION: 질문글
        * ANSWER: 답변글
      
      **응답 데이터**
      - QuestionDto: 좋아요 처리 결과
        * questionBoardLike: 좋아요 정보
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - FORBIDDEN (403): 본인 작성 글에 좋아요 시도
      - CONFLICT (409): 이미 좋아요한 게시글에 중복 요청
      - NOT_FOUND (404): 존재하지 않는 게시글 ID
      
      **참고사항**
      - 본인이 작성한 글에는 좋아요 불가
      - 중복 좋아요 방지 로직 적용
      - 좋아요 시 엽전 포인트 지급
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
      질문글 또는 답변의 좋아요를 취소합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - postId (필수): 좋아요 취소할 게시글 또는 답변의 ID (UUID)
      - contentType (필수): 컨텐츠 타입
        * QUESTION: 질문글
        * ANSWER: 답변글
      
      **응답 데이터**
      - 없음 (Void)
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - NOT_FOUND (404): 존재하지 않는 게시글 ID 또는 좋아요 기록 없음
      - FORBIDDEN (403): 다른 사용자의 좋아요 취소 시도
      
      **참고사항**
      - 본인이 좋아요한 게시글만 취소 가능
      - 좋아요 취소 시 엽전 포인트 회수
      - 취소된 좋아요는 재추가 가능
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
      summary = "답변글 등록",
      description = """
      질문글에 대한 답변을 등록합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - questionPostId (필수): 답변할 질문글 ID (UUID)
      - content (필수): 답변 본문
      - mediaFiles (선택): 첨부파일 목록 (최대 3개, 이미지만 지원)
      - isPrivate (선택): 내 정보 비공개 여부 (기본값: false)
      
      **응답 데이터**
      - QuestionDto: 등록된 답변 정보
        * answerPost: 답변글 정보
        * mediaFiles: 답변 첨부파일 목록
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - NOT_FOUND (404): 존재하지 않는 질문글 ID
      - INVALID_REQUEST (400): 필수 파라미터 누락 또는 유효하지 않은 데이터
      - FILE_UPLOAD_ERROR (400): 첨부파일 업로드 실패
      
      **참고사항**
      - 첨부파일은 이미지 파일만 지원
      - 답변 등록 시 질문 작성자에게 알림 발송
      - 답변 등록 시 엽전 포인트 지급
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
      summary = "특정 질문글의 답변 조회",
      description = """
      특정 질문글에 작성된 모든 답변을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - questionPostId (필수): 질문글 ID (UUID)
      
      **응답 데이터**
      - QuestionDto: 답변 목록 정보
        * answerPosts: 해당 질문글의 답변 목록
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - NOT_FOUND (404): 존재하지 않는 질문글 ID
      - FORBIDDEN (403): 비공개 게시글 접근 권한 없음
      
      **참고사항**
      - 답변이 없는 경우 빈 리스트 반환
      - 채택된 답변이 있는 경우 상단에 우선 표시
      - 답변 작성 시간순으로 정렬
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
      질문글에 대한 답변을 채택합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER (질문 작성자만 가능)
      
      **요청 파라미터**
      - postId (필수): 채택할 답변글 ID (UUID)
      
      **응답 데이터**
      - QuestionDto: 채택된 답변 정보
        * answerPost: 채택된 답변글 정보
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰 없음 또는 만료
      - FORBIDDEN (403): 질문 작성자가 아닌 사용자의 채택 시도
      - NOT_FOUND (404): 존재하지 않는 답변글 ID
      - CONFLICT (409): 본인 답변 채택 시도 또는 이미 채택된 답변 존재
      
      **참고사항**
      - 질문 작성자만 답변 채택 가능
      - 본인이 작성한 답변은 채택 불가
      - 채택 시 답변 작성자에게 엽전 현상금 지급
      - 하나의 질문글에는 하나의 답변만 채택 가능
      """
  )
  ResponseEntity<QuestionDto> chaetaekAnswerPost(
      CustomUserDetails customUserDetails,
      QuestionCommand command);
}
