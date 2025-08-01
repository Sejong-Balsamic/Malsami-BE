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
          **자료 글 등록 요청**

          **인증 : JWT 토큰 필요**

          #### 요청 파라미터
          - **`title`** (`String`, **필수**): 자료 게시글 제목
          - **`content`** (`String`, **필수**): 자료 게시글 본문
          - **`subject`** (`String`, **필수**): 교과목 명
          - **`documentTypes`** (`List<DocumentType>`, **선택**): 자료 유형 (최대 2개)
          - **`attendedYear`** (`Integer`, **선택**): 수강 년도
          - **`isDepartmentPrivate`** (`Boolean`, **선택**): 내 학과 비공개 여부 (기본값 = `false`)
          - **`attachmentFiles`** (`List<MultipartFile>`, **선택**): 첨부파일 
          - **`customTags`** (`List<String>`, **선택**): 커스텀태그

          ### **DocumentType**
          최대 2개까지의 카테고리를 설정 가능합니다.
          - **DOCUMENT**: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
          - **PAST_EXAM**: 퀴즈, 기출 문제, 과제 등
          - **SOLUTION**: 솔루션 등

          #### 반환 파라미터
            - **`DocumentPost documentPost`**: 자료 글 상세 정보
            - **`List<DocumentFile> documentFiles`**: 첨부파일 리스트
            - **`List<String> customTags`**: 커스텀 태그 리스트

          #### 참고 사항
          - 자료 글은 닉네임 비공개 기능이 없습니다.
          - 자료 글 등록 시 게시물 등급은 "천민" 등급으로 설정됩니다.
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
      summary = "일간 인기 자료글",
      description = """
          **자료 일간 인기글 요청**

          **이 API는 인증이 필요하지 않습니다.**

          #### 요청 파라미터
          `없음`

          ####반환 파라미터 값
          - **DocumentDto**: 자료 게시판 정보 반환
            - **Page\\<DocumentPost\\> documentPostsPage**: 일간 자료 인기글 리스트

          ####참고 사항
          - 이 API를 통해 사용자는 일간 인기 자료글을 조회할 수 있습니다.
          - 자료 게시판 일간 인기 점수는 매일 자정마다 초기화됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
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
      summary = "주간 인기 자료글",
      description = """
          **자료 주간 인기글 요청**

          **이 API는 인증이 필요하지 않습니다.**

          #### 요청 파라미터
          `없음`

          ####반환 파라미터 값
          - **DocumentDto**: 자료 게시판 정보 반환
            - **Page\\<DocumentPost\\> documentPostsPage**: 주간 자료 인기글 리스트

          ####참고 사항
          - 이 API를 통해 사용자는 주간 인기 자료글을 조회할 수 있습니다.
          - 자료 게시판 일간 인기 점수는 매주 월요일 자정마다 초기화됩니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
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
          **특정 자료 글 조회 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**
                    
          - **UUID documentPostId**: 자료 글 PK [필수]

          **반환 파라미터 값:**

          - **DocumentDto**: 자료 게시판 정보 반환
            - **DocumentPost documentPost**: 조회한 자료 글
            - **List<DocumentFile> documentFiles**: 자료글의 자료파일 리스트
             
          **참고 사항:**

          - 이 API를 통해 사용자는 자료 게시판에 작성된 글을 필터링하여 조회할 수 있습니다.
          - 사용자의 엽전 개수가 부족할 경우 접근이 제한될 수 있습니다.
          - 성공적으로 글 조회 시 사용자의 엽전이 자료 등급에 따라 감소합니다.
          - 성공적으로 글 조회 시 해당 자료 글의 조회수가 증가합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          - pageNumber = 3, pageSize = 10 입력시 4페이지에 해당하는 10개의 글을 반환합니다. (31번째 글 ~ 40번째 글 반환)
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
      summary = "자료게시판 자료 다운",
      description = """
          **자료게시판 자료 다운**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다**

          ### **요청 파라미터**
          - **`String documentFileId`** : 자료파일id

          ### **반환값**
          - **`byte[]`** : 파일 바이트 배열
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
          **자료게시판 Hot 다운로드**
                    
          **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다.**
                    
          ### **요청 파라미터**
          - **`pageNumber`** (`Integer`, 선택): 페이지 번호 (기본값 = 0)
                  
          - **`pageSize`** (`Integer`, 선택): 조회하고 싶은 주간 인기 질문 글 개수 (기본값 = 30)
                    
          ###반환 파라미터 값
          - **DocumentDto**: 자료 게시판 정보 반환
            - **Page\\<DocumentPost\\> documentPostsPage**: Hot 다운로드 순 자료 글
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
      summary = "자료 & 자료요청 게시판 좋아요/싫어요",
      description = """
          **자료 & 자료요청 글 좋아요/싫어요 증가**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **UUID documentPostId**: 좋아요/싫어요를 누른 자료글 or 자료요청글 PK [필수]
          
          - **ContentType contentType**: 자료글 or 자료요청글 [필수]
            _예: ContentType.DOCUMENT_
            _예: ContentType.DOCUMENT_REQUEST_
            
          - **ReactionType likeType**: 좋아요 or 싫어요 [필수]
            _예: ReactionType.LIKE_
            _예: ReactionType.DISLIKE_

          **반환 파라미터 값:**

          - **DocumentDto**: 자료 게시판 정보 반환
            - **DocumentBoardLike documentBoardLike**: 좋아요 내역

          **참고 사항:**

          - 이 API를 통해 사용자는 특정 자료 글에 좋아요 or 싫어요를 누를 수 있습니다.
          - 이 API를 통해 사용자는 특정 자료요청 글에 좋아요를 누를 수 있습니다.
          - 본인이 작성한 글에는 좋아요 or 싫어요를 누를 수 없습니다.
          - 이미 좋아요 or 싫어요를 누른 글 에는 중복으로 요청할 수 없습니다.
          - 특정 자료 등급에 접근 불가능한 사용자는 좋아요 or 싫어요 요청을 보낼 수 없습니다.
          - 자료요청 글은 '중인'이상 접근가능합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<DocumentDto> documentBoardLike(
      CustomUserDetails customUserDetails,
      DocumentCommand command);
}
