package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CourseCommand;
import com.balsamic.sejongmalsami.object.CourseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface CourseControllerDocs {

  @Operation(
      summary = "엑셀 파일 업로드",
      description = """
            ### 엑셀 파일 업로드 API
            
            **API 개요:**
            - 이 API는 세종대학교 교과목 정보를 엑셀 파일을 통해 업로드하고 저장합니다.
            - 교과과정은 6개월마다 (1학기, 2학기) 업데이트되므로 이 API는 자주 호출되지 않습니다.
            
            **업로드 파일 포맷:**
            - **파일명 형식:** `` `course-YYYY-S.xlsx` `` (예: `` `course-2024-2.xlsx` ``)
            - **파일 형식:** `` `.xlsx` `` 엑셀 파일
            - **데이터 구조:** 첫 번째 시트에 다음과 같은 컬럼이 포함되어야 합니다:
                1. **단과대학 (Faculty):** 문자열 (예: **"공과대학"**)
                2. **학과 (Department):** 문자열 (예: **"컴퓨터공학과"**)
                3. **교과목명 (Subject):** 문자열 (예: **"자료구조"**)
            - **특이 사항:** `` `법학부 법학전공` ``은 엑셀에서 **`"대학"`**으로 표기되어야 합니다.
            
            **비동기 처리:**
            - 이 API는 **비동기 방식**으로 처리됩니다. 요청이 수락되면 즉시 응답이 반환되며, 파일 처리는 백그라운드에서 진행됩니다.
            - 따라서, 반환값이 없지만 서버에서 파일 처리가 진행되고 있음을 이해해야 합니다.
            
            **입력 파라미터:**
            - **`` `sejongCourseFile (MultipartFile)` ``**: 업로드할 세종대학교 교과목명 엑셀 파일.
            
            **반환 파라미터:**
            - 반환값이 없지만, 요청이 성공적으로 수락되었음을 나타내는 **상태 코드**가 반환됩니다.
            """
  )
  ResponseEntity<CourseDto> uploadExcel(
      @ModelAttribute CourseCommand command);

  @Operation(
      summary = "단과대학 별 교과목명 조회",
      description = """
            ### 단과대학 별 교과목명 조회 API
            
            **API 개요:**
            - 이 API는 특정 단과대학에 속한 교과목명들을 조회합니다.
            - 교과목 정보는 주기적으로 업데이트되며, 이 API는 필요 시 호출됩니다.
            
            **입력 파라미터:**
            - **`` `CourseCommand` ``** 객체를 통해 다음과 같은 파라미터를 입력받습니다:
                - **`` `faculty (Faculty)` ``**: 조회할 단과대학의 이름. 예: **"소프트웨어융합대학"**
                  - **허용되는 값:**
                    - `` `대양휴머니티칼리지` ``
                    - `` `인문과학대학` ``
                    - `` `사회과학대학` ``
                    - `` `경영경제대학` ``
                    - `` `호텔관광대학` ``
                    - `` `자연과학대학` ``
                    - `` `생명과학대학` ``
                    - `` `전자정보공학대학` ``
                    - `` `소프트웨어융합대학` ``
                    - `` `공과대학` ``
                    - `` `예체능대학` ``
                    - `` `법학부법학전공` `` (엑셀에서 **"대학"**으로 표기)
                    - `` `연계전공` ``
                    - `` `인공지능융합대학` ``
            
            **반환 파라미터:**
            - **`` `CourseDto` ``** 객체를 반환하며, 다음과 같은 데이터를 포함합니다:
                - **`` `subjects (List<String>)` ``**: 해당 단과대학에 속한 교과목명 목록.
            """
  )
  ResponseEntity<CourseDto> getSubjectsByFaculty(
      @ModelAttribute CourseCommand command);
}
