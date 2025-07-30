package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.constants.HashType;
import com.balsamic.sejongmalsami.object.postgres.HashRegistry;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashRegistryRepository extends JpaRepository<HashRegistry, UUID> {
  Optional<HashRegistry> findByHashType(HashType hashType);
}
