package com.balsamic.sejongmalsami.object;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
public class CourseDto {
  // 교과목명 리스트
  List<String> subjects;

  List<Course> courses;
}
