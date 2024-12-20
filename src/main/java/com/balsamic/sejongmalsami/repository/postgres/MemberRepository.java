package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

  Optional<Member> findByStudentId(Long studentId);

  Boolean existsByStudentId(Long studentId);

  @Query("""
        SELECT m FROM Member m
        WHERE 
            (:studentId IS NULL OR m.studentId = :studentId)
            AND (:studentName IS NULL OR m.studentName LIKE %:studentName%)
            AND (:uuidNickname IS NULL OR m.uuidNickname LIKE %:uuidNickname%)
            AND (:major IS NULL OR m.major LIKE %:major%)
            AND (:academicYear IS NULL OR m.academicYear LIKE %:academicYear%)
            AND (:enrollmentStatus IS NULL OR m.enrollmentStatus LIKE %:enrollmentStatus%)
            AND (:accountStatus IS NULL OR m.accountStatus = :accountStatus)
            AND (:role IS NULL OR :role MEMBER OF m.roles)
            AND (:lastLoginStart = '' OR m.lastLoginTime >= CAST(:lastLoginStart AS timestamp))
            AND (:lastLoginEnd = '' OR m.lastLoginTime <= CAST(:lastLoginEnd AS timestamp))
            AND (:isFirstLogin IS NULL OR m.isFirstLogin = :isFirstLogin)
            AND (:isEdited IS NULL OR m.isEdited = :isEdited)
            AND (:isDeleted IS NULL OR m.isDeleted = :isDeleted)
        """)
  Page<Member> findAllDynamic(
      @Param("studentId") Long studentId,
      @Param("studentName") String studentName,
      @Param("uuidNickname") String uuidNickname,
      @Param("major") String major,
      @Param("academicYear") String academicYear,
      @Param("enrollmentStatus") String enrollmentStatus,
      @Param("accountStatus") AccountStatus accountStatus,
      @Param("role") Role role,
      @Param("lastLoginStart") String lastLoginStart,
      @Param("lastLoginEnd") String lastLoginEnd,
      @Param("isFirstLogin") Boolean isFirstLogin,
      @Param("isEdited") Boolean isEdited,
      @Param("isDeleted") Boolean isDeleted,
      Pageable pageable
  );

  @Query("""
SELECT m FROM Member m
LEFT join 
""")
  Page<Member> findMemberAndYeopjeon(
      @Param("studentId") String studentId,
      @Param("studentName") String studentName,
      @Param("memberId") String memberId
  );
}
