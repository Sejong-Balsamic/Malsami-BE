package com.balsamic.sejongmalsami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableMongoAuditing
@EnableAsync
@EnableScheduling
@EnableCaching
@EnableJpaRepositories(basePackages = "com.balsamic.sejongmalsami.repository.postgres")
@EnableMongoRepositories(basePackages = "com.balsamic.sejongmalsami.repository.mongo")
public class SejongMalsamiBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(SejongMalsamiBackendApplication.class, args);
  }

}
