package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.postgres.Faculty;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
  Optional<Faculty> findByName(String name);

  List<Faculty> findByNameIn(Set<String> name);

  List<Faculty> findByIsActiveTrue();
}
