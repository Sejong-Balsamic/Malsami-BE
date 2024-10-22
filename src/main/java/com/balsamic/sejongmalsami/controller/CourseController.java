package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CourseCommand;
import com.balsamic.sejongmalsami.object.CourseDto;
import com.balsamic.sejongmalsami.service.CourseService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course")
@Tag(
    name = "교과목 API",
    description = "교고목 관련 API 제공"
)
public class CourseController implements CourseControllerDocs {

  private final CourseService courseService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CourseDto> uploadExcel(
      @ModelAttribute CourseCommand command) {
    courseService.parseAndSaveCourses(command);
    return ResponseEntity.ok().build();
  }

  // 단과대학 별로 교과목명을 조회하는 API
  @PostMapping(value = "/subjects/faculty", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<CourseDto> getSubjectsByFaculty(
      @ModelAttribute CourseCommand command) {
    return ResponseEntity.ok(courseService.getSubjectsByFaculty(command));
  }
}
