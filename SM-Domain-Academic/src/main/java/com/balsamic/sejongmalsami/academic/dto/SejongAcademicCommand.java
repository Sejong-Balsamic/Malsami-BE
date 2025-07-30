package com.balsamic.sejongmalsami.academic.dto;

import com.balsamic.sejongmalsami.postgres.Faculty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Builder
public class SejongAcademicCommand {

  private MultipartFile sejongCourseFile;
  private Faculty faculty;
  private Integer year;
  private Integer semester;
}
