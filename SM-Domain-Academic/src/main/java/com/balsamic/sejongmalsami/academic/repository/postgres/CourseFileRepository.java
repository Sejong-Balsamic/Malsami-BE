package com.balsamic.sejongmalsami.academic.repository.postgres;

import com.balsamic.sejongmalsami.constants.FileStatus;
import com.balsamic.sejongmalsami.academic.object.postgres.CourseFile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseFileRepository extends JpaRepository<CourseFile, UUID> {
  Optional<CourseFile> findByFileName(String fileName);
  boolean existsByYearAndSemester(Integer year, Integer semester);

  List<CourseFile> findByFileStatus(FileStatus fileStatus);

  @Query("""
      select cf
      from CourseFile cf
      where (:year is null or cf.year = :year)
      and (:semester is null or cf.semester = :semester)
      """)
  Page<CourseFile> findAllByFiltered(
      @Param("year") Integer year,
      @Param("semester") Integer semester,
      Pageable pageable
  );
}
