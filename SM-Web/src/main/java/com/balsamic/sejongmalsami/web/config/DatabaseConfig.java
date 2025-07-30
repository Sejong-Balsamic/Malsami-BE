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
    "com.balsamic.sejongmalsami.repository.postgres"
})
@EnableMongoRepositories(basePackages = {
    "com.balsamic.sejongmalsami.repository.mongo"
})
@EntityScan(basePackages = {
    "com.balsamic.sejongmalsami.postgres",
    "com.balsamic.sejongmalsami.mongo"
})
public class DatabaseConfig {
} 