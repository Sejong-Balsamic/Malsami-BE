package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.SejongAcademicCommand;
import com.balsamic.sejongmalsami.object.SejongAcademicDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface SejongAcademicControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.12.10",
          author = Author.SUHSAECHAN,
          description = "단과대 목록 반환 구현"
      )
  })
  @Operation(
      summary = "단과대 목록 반환",
      description = """
          **단과대 목록 반환 메소드 API**

          **인증 : JWT 토큰 필요**

          #### 요청 파라미터
          - **`없음`**

          #### 반환 파라미터
            - **`List<Faculty> faculties`**: 단과대 목록

          #### 참고 사항
          - 나무위키에서 가져온 정보들과 옛날부터 쌓아진 세종대가 제공해준 모든 단과대 정보를 교집합한 결과입니다
          """
  )
  ResponseEntity<SejongAcademicDto> getAllFaculties(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute SejongAcademicCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.12.25",
          author = Author.SUHSAECHAN,
          description = "교과목명 목록 반환 구현"
      )
  })
  @Operation(
      summary = "교과목명 목록 반환",
      description = """
          **단과대 목록 반환 메소드 API**

          **인증 : 필요없음**

          #### 요청 파라미터
          - **`없음`**

          #### 반환 파라미터
            - **`List<String> subjects`**: 교과목명 목록
          """
  )
  ResponseEntity<SejongAcademicDto> getAllSubjects();
}
