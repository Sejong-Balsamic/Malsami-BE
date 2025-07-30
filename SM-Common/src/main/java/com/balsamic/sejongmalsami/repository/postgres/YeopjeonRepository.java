package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.postgres.Member;
import com.balsamic.sejongmalsami.postgres.Yeopjeon;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface YeopjeonRepository extends JpaRepository<Yeopjeon, UUID> {

  Optional<Yeopjeon> findByMember(Member member);

  @Query(value = """
      SELECT sub.rank
      FROM (
          SELECT member_member_id, RANK() OVER (ORDER BY yeopjeon DESC) AS rank
          FROM yeopjeon
      ) sub
      WHERE sub.member_member_id = :memberId
      """, nativeQuery = true)
  Integer findRankByMemberId(UUID memberId);

  @Query(value = "SELECT COUNT(*) FROM yeopjeon", nativeQuery = true)
  Integer findYeopjeonHolderCount();
}
