package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.CourseFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseFileRepository extends JpaRepository<CourseFile, UUID> {
  Optional<CourseFile> findByFileName(String fileName);
  boolean existsByYearAndSemester(Integer year, Integer semester);
}
