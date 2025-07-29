package com.balsamic.sejongmalsami.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
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
@ComponentScan(basePackages = {
    "com.balsamic.sejongmalsami.web",
    "com.balsamic.sejongmalsami.member",
    "com.balsamic.sejongmalsami.post", 
    "com.balsamic.sejongmalsami.notice",
    "com.balsamic.sejongmalsami.auth",
    "com.balsamic.sejongmalsami.academic",
    "com.balsamic.sejongmalsami.ai",
    "com.balsamic.sejongmalsami.application",
    "com.balsamic.sejongmalsami"
})
@EnableJpaRepositories(basePackages = "com.balsamic.sejongmalsami.repository.postgres")
@EnableMongoRepositories(basePackages = "com.balsamic.sejongmalsami.repository.mongo")
public class SejongMalsamiBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(SejongMalsamiBackendApplication.class, args);
  }

}
