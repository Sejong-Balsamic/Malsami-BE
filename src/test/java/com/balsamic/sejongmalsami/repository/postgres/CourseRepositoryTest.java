package com.balsamic.sejongmalsami.repository.postgres;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class CourseRepositoryTest {

  @Autowired
  private CourseRepository courseRepository;

  @Test
  void 학과_반환() {
//    Pageable pageable = PageRequest.of(0, 2);
//    Page<Course> coursePage = courseRepository.findAll(pageable);
//    log.info("############################################################");
//    log.info("Page<Course> : {}", coursePage.getContent());
//    log.info("############################################################");
//
//    List<Course> courses = courseRepository.findAllBySubject("컴퓨터구조");
//
//    List<Faculty> faculties = courses.stream()
//        .map(Course::getFaculty)
//        .toList();
//
//    log.info("course : {}", courses);
//    log.info("faculty : {}", faculties);
  }
}