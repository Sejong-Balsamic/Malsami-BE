package com.balsamic.sejongmalsami;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SejongMalsamiBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(SejongMalsamiBackendApplication.class, args);
  }

}
