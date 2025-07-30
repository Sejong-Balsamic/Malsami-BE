package com.balsamic.sejongmalsami.academic.repository.postgres;

import com.balsamic.sejongmalsami.academic.object.postgres.DepartmentFile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentFileRepository extends JpaRepository<DepartmentFile, UUID> {
  Optional<DepartmentFile> findByFileName(String fileName);
}
