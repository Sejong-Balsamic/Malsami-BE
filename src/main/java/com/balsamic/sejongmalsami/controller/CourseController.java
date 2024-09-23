package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CourseController {

  private final CourseService courseService;

  public CourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  @PostMapping("/upload-excel")
  @Operation(
      summary = "엑셀 파일 업로드",
      description = """
      **엑셀 파일 업로드**

      파일명을 입력받아 엑셀 파일을 통해 교과목 정보를 저장하는 API입니다.

      **입력 파라미터 값:**

      - **`String file`**: 업로드할 엑셀 파일의 이름  
        _예: "course-2024-2.xlsx"_

      **DB에 저장되는 정보:**

      - **`UUID courseId`**: course 아이디
      - **`String subject`**: 교과목명
      - **`Faculty faculty`**: 단과대학
      - **`String department`**: 학년
      - **`LocalDateTime createdDate`**: 생성 날짜
      - **`LocalDateTime updatedDate`**: 업데이트 날짜
      - **`Boolean isDeleted`**: 삭제 여부
      
      **반환 파라미터 값:**

      - 없음
      """
  )
  public ResponseEntity<Void> uploadExcel(@RequestParam("file") String file) throws IOException {
    courseService.saveCoursesFromExcel(file);
    return ResponseEntity.ok().build();
  }

  // 단과대학 별로 교과목명을 조회하는 API
  @PostMapping("/subjects-by-faculty")
  @Operation(
      summary = "단과대학 별 교과목명 조회",
      description = """
        **단과대학 별 교과목명 조회**

        특정 단과대학에 속한 교과목명들을 조회하는 API입니다.

        **입력 파라미터 값:**

        - **`String faculty`**: 단과대학 이름  
          _예: "소프트웨어융합대학"_

        **DB에서 조회되는 정보:**  
        
        - **`List<String>`**: 교과목명들의 리스트

        **반환 파라미터 값:**

        - **`List<String>`**: 해당 단과대학에 속한 교과목명 목록  
        """
  )
  public ResponseEntity<List<String>> getSubjectsByFaculty(@RequestParam("faculty") String faculty) {
    return ResponseEntity.ok(courseService.getSubjectsByFaculty(faculty));
  }
}
