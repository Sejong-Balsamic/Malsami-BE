package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.Faculty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Builder
public class CourseCommand {

  private MultipartFile sejongCourseFile;
  private Faculty faculty;
  private Integer year;
  private Integer semester;
}
