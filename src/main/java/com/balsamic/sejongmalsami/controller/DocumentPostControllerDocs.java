package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface DocumentPostControllerDocs {

  @ApiChangeLogs({
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
          **글 등록 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **String title**: 자료게시글 제목 (필수)
            _예: "컴퓨터구조 과제5 로지심파일"_

          - **String content**: 자료게시글 본문 (필수)
            _예: "컴구 과제5 로지심파일 입니다."_
            
          - **String subject**: 교과목 명 (필수)
            _예: "컴퓨터구조"_
            
          - **Enum documentTypeSet**: 자료 카테고리 (최대 2개까지만 선택가능)
            _예: "SOLUTION"_
            
          - **Boolean isDepartmentPrivate**: 내 학과 비공개 여부 (default = false)
            _기본값은 false입니다. true로 요청할 시 자료 글에 내 학과가 비공개 처리됩니다._

          **자료 카테고리**
                    
          총 3개의 자료 카테고리가 존재하며 최대 2개까지의 카테고리를 설정할 수 있습니다.
          - **DOCUMENT** (자료: 필기 자료, 교안, 녹화본, 실험/실습 자료)
          - **PAST_EXAM** (기출: 퀴즈, 기출 문제, 과제)
          - **SOLUTION** (해설: 솔루션)
                    
            _예: "formData.append('documentTypeSet', 'SOLUTION');_

          **반환 파라미터 값:**

          - **DocumentDto**: 자료 게시판 정보 반환
            - **DocumentPost documentPost**: 자료 글 정보
            - **DocumentRequestPost documentRequestPost**: null

          **참고 사항:**

          - 이 API를 통해 사용자는 자료게시판에 자료 글을 동록할 수 있습니다.
          - 글 제목, 본문, 과목명은 null 값이 들어갈 수 없습니다. (required)
          - 내 학과 비공개 여부는 프론트에서 설정하지 않으면 default 값이 할당됩니다.
          - 자료 글은 닉네임 비공개 기능이 없습니다.
          - 자료 글 등록 시 게시물 등급은 "천민" 등급으로 자동 설정됩니다.
          - 성공적인 등록 후, 등록 된 자료글을 반환합니다.
          """
  )
  ResponseEntity<DocumentDto> saveDocumentPost(
      CustomUserDetails customUserDetails,
      DocumentCommand command);

  @ApiChangeLogs({
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

          - **List<DocumentDto>**: 일간 인기 자료글 List 반환
            - **DocumentPost documentPost**: 자료 글 정보
            - **DocumentRequestPost documentRequestPost**: null

          **참고 사항:**

          - 이 API를 통해 사용자는 일간 인기 자료글을 조회할 수 있습니다.
          - 요청 시각으로부터 24시간 이내에 작성된 상위 30개의 일간 인기글을 조회합니다.
          """
  )
  ResponseEntity<List<DocumentDto>> getDailyPopularDocumentPost(
      DocumentCommand command);

  @ApiChangeLogs({
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

          - **List<DocumentPostDto>**: 주간 인기 자료글 List 반환
            - **DocumentPost documentPost**: 자료 글 정보
            - **DocumentRequestPost documentRequestPost**: null

          **참고 사항:**

          - 이 API를 통해 사용자는 주간 인기 자료글을 조회할 수 있습니다.
          - 요청 시각으로부터 7일 이내에 작성된 상위 30개의 주간 인기글을 조회합니다.
          """
  )
  ResponseEntity<List<DocumentDto>> getWeeklyPopularDocumentPost(
      DocumentCommand command);
}
