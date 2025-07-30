package com.balsamic.sejongmalsami.web.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

// 멀티모듈 데이터베이스 설정
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {
    "com.balsamic.sejongmalsami.repository.postgres",
    "com.balsamic.sejongmalsami.post.repository.postgres",
    "com.balsamic.sejongmalsami.notice.repository.postgres",
    "com.balsamic.sejongmalsami.ai.repository.postgres",
    "com.balsamic.sejongmalsami.academic.repository.postgres"
})
@EnableMongoRepositories(basePackages = {
    "com.balsamic.sejongmalsami.repository.mongo",
    "com.balsamic.sejongmalsami.post.repository.mongo",
    "com.balsamic.sejongmalsami.notice.repository.mongo",
    "com.balsamic.sejongmalsami.auth.repository.mongo",
    "com.balsamic.sejongmalsami.ai.repository.mongo"
})
@EntityScan(basePackages = {
    "com.balsamic.sejongmalsami.object.postgres",
    "com.balsamic.sejongmalsami.object.mongo",
    "com.balsamic.sejongmalsami.post.object.postgres",
    "com.balsamic.sejongmalsami.post.object.mongo",
    "com.balsamic.sejongmalsami.notice.object.postgres",
    "com.balsamic.sejongmalsami.notice.object.mongo",
    "com.balsamic.sejongmalsami.auth.object.mongo",
    "com.balsamic.sejongmalsami.ai.object.postgres",
    "com.balsamic.sejongmalsami.ai.object.mongo",
    "com.balsamic.sejongmalsami.academic.object.postgres"
})
public class DatabaseConfig {
} 