package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.Report;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
}
