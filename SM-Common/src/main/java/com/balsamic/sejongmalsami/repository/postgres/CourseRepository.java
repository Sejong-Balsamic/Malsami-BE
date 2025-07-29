package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  // 교과목 필터링
  @Query("""
      select c
      from Course c
      where (:subject is null or c.subject like %:subject%)
      and (:faculty is null or c.faculty = :faculty)
      and (:year is null or c.year = :year)
      and (:semester is null or c.semester = :semester)
      """)
  Page<Course> findAllByFiltered(
      @Param("subject") String subject,
      @Param("faculty") String faculty,
      @Param("year") Integer year,
      @Param("semester") Integer semester,
      Pageable pageable
  );

  // 특정 단어가 포함된 Course 리스트 반환
  List<Course> findBySubjectContainingIgnoreCase(String keyword);

  // 교과목 연도 조회 (내림차순)
  @Query("select distinct c.year from Course c order by c.year desc")
  List<Integer> findDistinctYears();

  // 교과목 학기 조회 (오름차순)
  @Query("select distinct c.semester from Course c order by c.semester asc")
  List<Integer> findDistinctSemesters();
}
