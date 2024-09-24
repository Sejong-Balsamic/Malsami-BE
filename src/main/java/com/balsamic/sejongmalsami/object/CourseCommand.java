package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.Faculty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
@Builder
public class CourseCommand {
  private MultipartFile sejongCourseFile;
  private Faculty faculty;
  private int year;
  private int semester;
}
