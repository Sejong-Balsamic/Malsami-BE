package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.ServerErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerErrorCodeRepository extends JpaRepository<ServerErrorCode, UUID> {

  Optional<ServerErrorCode> findByErrorCode(String errorCode);
}
