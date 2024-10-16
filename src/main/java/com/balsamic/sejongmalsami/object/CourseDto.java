package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Course;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Builder
public class CourseDto {

  // 교과목명 리스트
  List<String> subjects;

  List<Course> courses;
}
