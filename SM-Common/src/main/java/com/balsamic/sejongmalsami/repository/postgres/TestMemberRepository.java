package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.TestMember;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TestMemberRepository extends JpaRepository<TestMember, UUID> {

  Optional<TestMember> findByTestStudentId(Long studentId);

  @Query("""
      SELECT tm from TestMember tm
      WHERE
      (:testStudentId is null or tm.testStudentId = :testStudentId)
      AND (:testStudentName IS NULL OR tm.testStudentName LIKE %:testStudentName%)
      """)
  Page<TestMember> findAllDynamic(Long testStudentId, String testStudentName, Pageable pageable);
}
