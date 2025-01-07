package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.FileStatus;
import com.balsamic.sejongmalsami.object.postgres.CourseFile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseFileRepository extends JpaRepository<CourseFile, UUID> {
  Optional<CourseFile> findByFileName(String fileName);
  boolean existsByYearAndSemester(Integer year, Integer semester);

  List<CourseFile> findByFileStatus(FileStatus fileStatus);
}
