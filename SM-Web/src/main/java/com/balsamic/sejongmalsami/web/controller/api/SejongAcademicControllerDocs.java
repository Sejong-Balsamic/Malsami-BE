package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.academic.dto.SejongAcademicCommand;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.object.SejongAcademicDto;
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
      summary = "단과대 목록 조회",
      description = """
      세종대학교의 모든 단과대 목록을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - SejongAcademicDto: 단과대 정보
        * faculties: 전체 단과대 목록
        * facultyName: 단과대명
        * facultyCode: 단과대 코드
      
      **예외 상황**
      - UNAUTHORIZED (401): 인증 토큰이 유효하지 않음
      - INTERNAL_SERVER_ERROR (500): 단과대 데이터 조회 실패
      
      **참고사항**
      - 나무위키 및 세종대 공식 데이터를 기반으로 구성된 신뢰성 있는 데이터
      - 게시글 작성 시 단과대 선택을 위해 사용
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
      summary = "교과목명 목록 조회",
      description = """
      세종대학교의 모든 교과목명 목록을 조회합니다.
      
      **인증 요구사항**
      - 인증 필요: 없음
      - 권한: 공개 API
      
      **요청 파라미터**
      - 없음
      
      **응답 데이터**
      - SejongAcademicDto: 교과목 정보
        * subjects: 전체 교과목명 목록
        * subjectName: 교과목명
        * subjectCode: 교과목 코드
      
      **예외 상황**
      - INTERNAL_SERVER_ERROR (500): 교과목 데이터 조회 실패
      
      **참고사항**
      - 인증 없이 접근 가능한 공개 API
      - 게시글 작성 시 교과목 선택을 위해 사용
      - 세종대 공식 데이터를 기반으로 구성
      """
  )
  ResponseEntity<SejongAcademicDto> getAllSubjects();
}
