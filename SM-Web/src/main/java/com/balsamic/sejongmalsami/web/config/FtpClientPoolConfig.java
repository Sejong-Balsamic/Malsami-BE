package com.balsamic.sejongmalsami.web.config;

import com.balsamic.sejongmalsami.util.properties.FtpProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FtpClientPoolConfig {

  private final FtpProperties ftpConfig;

  @Bean
  public GenericObjectPool<FTPClient> ftpClientPool() {
    FtpClientFactory factory = new FtpClientFactory(ftpConfig);

    GenericObjectPoolConfig<FTPClient> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setMaxTotal(10); // 최대 연결 수 설정
    poolConfig.setMinIdle(2);    // 최소 유휴 연결 수 설정
    poolConfig.setMaxIdle(5);    // 최대 유휴 연결 수 설정
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    poolConfig.setMinEvictableIdleTimeMillis(300000); // 5분

    // JMX MBean 등록 비활성화
    poolConfig.setJmxEnabled(false);

    // 유휴 연결 검사 주기 설정 (예: 5분)
    poolConfig.setTimeBetweenEvictionRunsMillis(300000);
    poolConfig.setNumTestsPerEvictionRun(3);

    return new GenericObjectPool<>(factory, poolConfig);
  }
}
