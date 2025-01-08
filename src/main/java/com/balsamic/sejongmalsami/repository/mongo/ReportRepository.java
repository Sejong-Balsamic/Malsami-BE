package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.Report;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {

  boolean existsByReporterIdAndReportedIdAndReportedEntityId(UUID reporterId, UUID reportedId, UUID reportedEntityId);
}
