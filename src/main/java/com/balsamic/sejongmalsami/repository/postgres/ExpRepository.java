package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpRepository extends JpaRepository<Exp, UUID> {

  Optional<Exp> findByMember(Member member);

}
