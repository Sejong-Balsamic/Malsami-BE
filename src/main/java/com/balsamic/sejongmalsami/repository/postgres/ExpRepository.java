package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpRepository extends JpaRepository<Exp, UUID> {

  Optional<Exp> findByMember(Member member);

  @Query(value = """
        SELECT sub.rank
        FROM (
            SELECT member_member_id, RANK() OVER (ORDER BY exp DESC) AS rank
            FROM exp
        ) sub
        WHERE sub.member_member_id = :memberId
        """, nativeQuery = true)
  Integer findRankByMemberId(UUID memberId);

  @Query(value = "SELECT COUNT(*) FROM exp", nativeQuery = true)
  Integer findExpHolderCount();
}
