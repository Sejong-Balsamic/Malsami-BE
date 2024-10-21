package com.balsamic.sejongmalsami.util.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FtpClientPoolConfig {

  private final FtpConfig ftpConfig;

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
    poolConfig.setMinEvictableIdleTimeMillis(60000); // 60초

    // JMX MBean 등록 비활성화
    poolConfig.setJmxEnabled(false);

    return new GenericObjectPool<>(factory, poolConfig);
  }
}
