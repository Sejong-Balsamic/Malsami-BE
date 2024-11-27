package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

  // 단과대학 이름으로 중복 없는 교과목명 목록 조회
  @Query("SELECT DISTINCT c.subject FROM Course c WHERE c.faculty = :faculty")
  List<String> findDistinctSubjectByFaculty(Faculty faculty);

  boolean existsByYearAndSemester(int year, int semester);

  // 교과목 명을 통해 단과대 조회
  List<Course> findAllBySubject(String subject);

  void deleteByYearAndSemester(Integer year, Integer semester);
}
