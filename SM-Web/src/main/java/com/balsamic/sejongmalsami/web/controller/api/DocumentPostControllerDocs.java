package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.post.dto.DocumentCommand;
import com.balsamic.sejongmalsami.post.dto.DocumentDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface DocumentPostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.11.29",
          author = Author.SUHSAECHAN,
          description = "DocumentPost 파라미터 추가 : documentType 필수로 변경, attendedYear, 및 커스텀태그로직추가"
      ),
      @ApiChangeLog(
          date = "2024.11.22",
          author = Author.SUHSAECHAN,
          description = "첨부자료 로직 전체적으로 리펙토링"
      ),
      @ApiChangeLog(
          date = "2024.10.28",
          author = Author.SUHSAECHAN,
          description = "파일 유효성 검사 구체화 : UploadType 검증 및 업로드 크기 제한"
      ),
      @ApiChangeLog(
          date = "2024.10.24",
          author = Author.SUHSAECHAN,
          description = "자료 업로드 및 썸네일 로직 전체 구조 개선 및 업로드 로직 간편화"
      ),
      @ApiChangeLog(
          date = "2024.10.22",
          author = Author.SUHSAECHAN,
          description = "자료 업로드 및 썸네일 로직 추가"
      ),
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.14",
          author = Author.BAEKJIHOON,
          description = "자료게시판 command, dto 통합"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "자료게시판 글 등록"
      )
  })
  @Operation(
      summary = "자료 글 등록",
      description = """
      자료 게시판에 새로운 자료 글을 등록합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - title (필수): 자료 게시글 제목
      - content (필수): 자료 게시글 본문
      - subject (필수): 교과목 명
      - documentTypes (선택): 자료 유형 (최대 2개)
        * DOCUMENT: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
        * PAST_EXAM: 퀴즈, 기출 문제, 과제 등
        * SOLUTION: 솔루션 등
      - attendedYear (선택): 수강 년도
      - isDepartmentPrivate (선택): 내 학과 비공개 여부 (기본값: false)
      - attachmentFiles (선택): 첨부파일 목록
      - customTags (선택): 커스텀 태그 목록
      
      **응답 데이터**
      - DocumentDto: 등록된 자료 글 정보
        * documentPost: 자료 글 상세 정보
        * documentFiles: 첨부파일 리스트
        * customTags: 커스텀 태그 리스트
      
      **예외 상황**
      - TITLE_REQUIRED (400): 제목이 필요함
      - CONTENT_REQUIRED (400): 내용이 필요함
      - SUBJECT_REQUIRED (400): 교과목명이 필요함
      - FILE_SIZE_EXCEEDED (400): 파일 크기 초과
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - 자료 글은 닉네임 비공개 기능이 없음
      - 자료 글 등록 시 게시물 등급은 천민 등급으로 설정
      - 첨부파일은 유효성 검사를 통과해야 함
      """
  )
  ResponseEntity<DocumentDto> saveDocumentPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

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
          description = "자료 글 일간 인기점수 24시간마다 초기화"
      ),
      @ApiChangeLog(
          date = "2024.11.15",
          author = Author.SUHSAECHAN,
          description = "자료 게시글 최근 5년간 글중에 dailyScore 큰 순으로 Pageable 반환으로 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "자료게시판 일간 인기글 init"
      )
  })
  @Operation(
      summary = "일간 인기 자료 조회",
      description = """
      하루 동안 가장 인기가 높았던 자료 게시글을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - DocumentDto: 일간 인기 자료 정보
        * documentPostsPage: 일간 인기 점수 순 자료 게시글 목록
        * dailyScore: 일간 인기 점수
        * viewCount: 일간 조회수
        * likeCount: 일간 좋아요 수
      
      **예외 상황**
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 인증 없이 접근 가능한 공개 API
      - 일간 인기 점수는 매일 자정에 초기화
      - 인기 점수는 조회수, 좋아요, 댓글 등을 종합하여 계산
      - 최근 5년간 작성된 자료만 대상
      """
  )
  ResponseEntity<DocumentDto> getDailyPopularDocumentPost();

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
          description = "자료 글 주간 인기점수 7일마다 초기화"
      ),
      @ApiChangeLog(
          date = "2024.11.15",
          author = Author.SUHSAECHAN,
          description = "자료 게시글 최근 5년간 글중에 weeklyScore 큰 순으로 Pageable 반환으로 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "자료게시판 주간 인기글 init"
      )
  })
  @Operation(
      summary = "주간 인기 자료 조회",
      description = """
      일주일 동안 가장 인기가 높았던 자료 게시글을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - DocumentDto: 주간 인기 자료 정보
        * documentPostsPage: 주간 인기 점수 순 자료 게시글 목록
        * weeklyScore: 주간 인기 점수
        * viewCount: 주간 조회수
        * likeCount: 주간 좋아요 수
      
      **예외 상황**
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 인증 없이 접근 가능한 공개 API
      - 주간 인기 점수는 매주 월요일 자정에 초기화
      - 인기 점수는 조회수, 좋아요, 댓글 등을 종합하여 계산
      - 최근 5년간 작성된 자료만 대상
      """
  )
  ResponseEntity<DocumentDto> getWeeklyPopularDocumentPost();

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.12.10",
          author = Author.SUHSAECHAN,
          description = "자료반환시 documentFile 리스트 반환"
      ),
      @ApiChangeLog(
          date = "2024.11.20",
          author = Author.BAEKJIHOON,
          description = "특정 자료 글 조회"
      )
  })
  @Operation(
      summary = "특정 자료 글 조회",
      description = """
      특정 자료 게시글의 상세 정보를 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - documentPostId (필수): 조회할 자료 글 ID
      
      **응답 데이터**
      - DocumentDto: 자료 글 상세 정보
        * documentPost: 조회한 자료 글
        * documentFiles: 자료글의 자료파일 리스트
      
      **예외 상황**
      - DOCUMENT_POST_NOT_FOUND (404): 자료 글을 찾을 수 없음
      - INSUFFICIENT_YEAPJEON (400): 엽전이 부족함
      - ACCESS_DENIED (403): 접근 권한이 없음
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - 사용자의 엽전 개수가 부족할 경우 접근 제한
      - 성공적으로 글 조회 시 사용자의 엽전이 자료 등급에 따라 감소
      - 성공적으로 글 조회 시 해당 자료 글의 조회수가 증가
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      """
  )
  ResponseEntity<DocumentDto> getDocumentPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "API 인증 생략"
      ),
      @ApiChangeLog(
          date = "2024.11.30",
          author = Author.SUHSAECHAN,
          description = "member 정보 없어도 접근 가능한 API여야함 : 자료티어 확인 로직 제거"
      ),
      @ApiChangeLog(
          date = "2024.11.21",
          author = Author.BAEKJIHOON,
          description = "단과대 필터링 추가"
      ),
      @ApiChangeLog(
          date = "2024.11.20",
          author = Author.BAEKJIHOON,
          description = "자료게시판 필터링 조회 수정"
      ),
      @ApiChangeLog(
          date = "2024.11.04",
          author = Author.SUHSAECHAN,
          description = "자료게시판 필터링 조회 기본 구현"
      )
  })
  @Operation(
      summary = "자료글 필터링 조회",
      description = """
      다양한 조건으로 자료 게시글을 필터링하여 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - subject (선택): 교과목명 필터링
      - documentTypes (선택): 자료 유형 필터링 (최대 2개)
        * DOCUMENT: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
        * PAST_EXAM: 퀴즈, 기출 문제, 과제 등
        * SOLUTION: 솔루션 등
      - faculty (선택): 단과대 필터링
      - postTier (선택): 자료 등급별 필터링
      - sortType (선택): 정렬 기준 (기본값: 최신순)
        * LATEST: 최신순
        * MOST_LIKED: 좋아요순
        * VIEW_COUNT: 조회수 순
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지 크기 (기본값: 30)
      
      **응답 데이터**
      - DocumentDto: 자료 게시판 정보
        * documentPostsPage: 필터링된 자료글 리스트
      
      **예외 상황**
      - INVALID_PAGE_REQUEST (400): 잘못된 페이지 요청
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 인증 없이 접근 가능한 공개 API
      - 여러 필터를 조합하여 원하는 자료 검색 가능
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      - 페이지 번호는 0부터 시작
      """
  )
  ResponseEntity<DocumentDto> filteredDocumentPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);


  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.12.11",
          author = Author.SUHSAECHAN,
          description = "내 자료 다운로드시 차감 X, 엽전내역 남지않음 , DocumentFile에서 전체 총 다운로드수, 주간 다운로드수, 일간다운로드수 정의"
      ),
      @ApiChangeLog(
          date = "2024.12.01",
          author = Author.SUHSAECHAN,
          description = "파일 다운로드 로직 구현"
      )
  })
  @Operation(
      summary = "자료 파일 다운로드",
      description = """
      자료 게시판의 첨부파일을 다운로드합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - documentFileId (필수): 다운로드할 자료파일 ID
      
      **응답 데이터**
      - byte[]: 파일 바이트 배열
        * 파일 내용이 바이너리 형태로 반환됨
      
      **예외 상황**
      - DOCUMENT_FILE_NOT_FOUND (404): 자료파일을 찾을 수 없음
      - INSUFFICIENT_YEAPJEON (400): 엽전이 부족함
      - FILE_DOWNLOAD_ERROR (500): 파일 다운로드 오류
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - 내 자료 다운로드 시 엽전 차감 없음
      - 다운로드 시 해당 파일의 다운로드 수가 증가
      - 파일 크기에 따라 응답 시간이 달라질 수 있음
      """
  )
  ResponseEntity<byte[]> downloadDocumentFile(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.12.04",
          author = Author.BAEKJIHOON,
          description = "Hot 다운로드 구현"
      )
  })
  @Operation(
      summary = "Hot 다운로드 조회",
      description = """
      다운로드가 많은 인기 자료 게시글을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지 크기 (기본값: 30)
      
      **응답 데이터**
      - DocumentDto: 자료 게시판 정보
        * documentPostsPage: Hot 다운로드 순 자료 글 목록
      
      **예외 상황**
      - INVALID_PAGE_REQUEST (400): 잘못된 페이지 요청
      - UNAUTHORIZED (401): 인증이 필요함
      - INTERNAL_SERVER_ERROR (500): 서버 내부 오류
      
      **참고사항**
      - 다운로드 수가 높은 순으로 정렬
      - 페이지 번호는 0부터 시작
      - 인기 자료를 빠르게 찾을 수 있는 기능
      """
  )
  ResponseEntity<DocumentDto> getHotDownload(
      DocumentCommand command
  );

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.03.21",
          author = Author.BAEKJIHOON,
          description = "LikeController -> DocumentPostController API 이관"
      ),
      @ApiChangeLog(
          date = "2024.11.21",
          author = Author.BAEKJIHOON,
          description = "자료 및 자료요청 게시판 좋아요/싫어요 init"
      )
  })
  @Operation(
      summary = "자료 게시글 좋아요/싫어요",
      description = """
      자료 게시글 또는 자료요청 게시글에 좋아요/싫어요를 추가합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - documentPostId (필수): 좋아요/싫어요를 누를 자료글 또는 자료요청글 ID
      - contentType (필수): 게시글 유형
        * DOCUMENT: 자료글
        * DOCUMENT_REQUEST: 자료요청글
      - likeType (필수): 좋아요/싫어요 타입
        * LIKE: 좋아요
        * DISLIKE: 싫어요
      
      **응답 데이터**
      - DocumentDto: 자료 게시판 정보
        * documentBoardLike: 좋아요/싫어요 내역
      
      **예외 상황**
      - DOCUMENT_POST_NOT_FOUND (404): 자료 글을 찾을 수 없음
      - SELF_LIKE_NOT_ALLOWED (400): 본인 글에는 좋아요/싫어요 불가
      - DUPLICATE_LIKE (400): 이미 좋아요/싫어요를 누른 글
      - ACCESS_DENIED (403): 특정 자료 등급에 접근 불가능
      - UNAUTHORIZED (401): 인증이 필요함
      
      **참고사항**
      - 본인이 작성한 글에는 좋아요/싫어요 불가
      - 중복 좋아요/싫어요 방지
      - 자료요청 글은 중인 이상 접근가능
      - Swagger 테스트 시 mediaFiles의 Send empty value 체크박스 해제 필요
      """
  )
  ResponseEntity<DocumentDto> documentBoardLike(
      CustomUserDetails customUserDetails,
      DocumentCommand command);
}
