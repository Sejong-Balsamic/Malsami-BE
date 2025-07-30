package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.ServerErrorCode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServerErrorCodeRepository extends JpaRepository<ServerErrorCode, UUID> {

  Optional<ServerErrorCode> findByErrorCode(String errorCode);

  @Query("""
    SELECT s
    FROM ServerErrorCode s
    WHERE
       (:errorCode IS NULL OR s.errorCode LIKE %:errorCode%)
       AND (:httpStatusCode IS NULL OR s.httpStatusCode = :httpStatusCode)
       AND (:httpStatusMessage IS NULL OR s.httpStatusMessage LIKE %:httpStatusMessage%)
       AND (:message IS NULL OR s.message LIKE %:message%)
  """)
  Page<ServerErrorCode> findAllDynamic(
      @Param("errorCode") String errorCode,
      @Param("httpStatusCode") Integer httpStatusCode,
      @Param("httpStatusMessage") String httpStatusMessage,
      @Param("message") String message,
      Pageable pageable
  );
}
