package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YeopjeonRepository extends JpaRepository<Yeopjeon, UUID> {

  Optional<Yeopjeon> findByMember(Member member);
}
