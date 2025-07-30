package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.postgres.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
  Optional<Subject> findByName(String name);
  boolean existsByName(String name);
  @Query("SELECT DISTINCT s.name FROM Subject s")
  List<String> findDistinctSubjectNames();
}
