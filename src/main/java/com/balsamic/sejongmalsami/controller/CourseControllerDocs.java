package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CourseCommand;
import com.balsamic.sejongmalsami.object.CourseDto;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

public interface CourseControllerDocs {

  //TODO: 어디에서 가져오는 파일인지 적어주세요
  @Operation(
      summary = "엑셀 파일 업로드",
      description = """
      **엑셀 파일 업로드**

      파일명을 입력받아 엑셀 파일을 통해 교과목 정보를 저장하는 API입니다.

      **입력 파라미터 값:**

      - **`MultipartFile sejongCourseFile`**: 업로드하는 세종대학교 교과목명 파일 
      
      **반환 파라미터 값:**

      - 없음
      """
  )
  ResponseEntity<CourseDto> uploadExcel(
      @ModelAttribute CourseCommand command);


  // 단과대학 별로 교과목명을 조회하는 API
  @PostMapping("/subjects-by-faculty")
  @Operation(
      summary = "단과대학 별 교과목명 조회",
      description = """
        **단과대학 별 교과목명 조회**

        특정 단과대학에 속한 교과목명들을 조회하는 API입니다.

        **입력 파라미터 값:**

        - **`Faculty faculty`**: 단과대학 이름  
          _예: "소프트웨어융합대학"_

        **반환 파라미터 값:**

        - **`List<String>`**: 해당 단과대학에 속한 교과목명 목록  
        """
  )
  public ResponseEntity<CourseDto> getSubjectsByFaculty(
      @ModelAttribute CourseCommand command);
}
