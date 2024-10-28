package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface DocumentPostControllerDocs {

  @ApiChangeLogs({
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
          description = "자료게시판 글 등록 & 인기글"
      )
  })
  @Operation(
      summary = "자료 글 등록",
      description = """
        **자료 글 등록 요청**

        이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다. 클라이언트는 Bearer 토큰을 통해 인증을 수행해야 합니다.

        ### **요청 파라미터**
        
        - **`title`** (`String`, **필수**)
        
        - **`content`** (`String`, **필수**)
        
        - **`subject`** (`String`, **필수**): 교과목명
        
        - **`documentTypeSet`** (`String`, **선택**)
        
        - **`isDepartmentPrivate`** (`Boolean`, **선택**): 내 학과 비공개 여부 : 기본값 `false` (공개)
       
        - **`attachmentFiles`** (`MultipartFile`, **선택**): 업로드 자료 파일 리스트
        
        ### **documentTypeSet**

        최대 2개까지의 카테고리를 설정 가능

        - **DOCUMENT**: 필기 자료, 교안, 녹화본, 실험/실습 자료 등
        - **PAST_EXAM**: 퀴즈, 기출 문제, 과제 등
        - **SOLUTION**: 솔루션 등

        ### **반환 파라미터 값**

        - **`DocumentDto`**: 자료 게시판 정보 반환
          - **`DocumentPost documentPost`**: 자료 글 정보

        ### **참고 사항**

        - 자료 글은 닉네임 비공개 기능이 없습니다.
        - 자료 글 등록 시 게시물 등급은 "천민" 등급 
        - 개별 파일의 최대 크기는 50MB
        """
  )
  ResponseEntity<DocumentDto> saveDocumentPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
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

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          없음

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

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          없음

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
