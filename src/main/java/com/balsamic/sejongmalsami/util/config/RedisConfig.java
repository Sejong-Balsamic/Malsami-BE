package com.balsamic.sejongmalsami.util.config;

import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.connection-pool-size}")
  private int connectionPoolSize;

  @Value("${spring.data.redis.connection-minimum-size}")
  private int connectionMinimumSize;

  @Value("${spring.data.redis.port}")
  private String redisPort;

  @Value("${spring.data.redis.password}")
  private String redisPassword;

  private static final int CACHE_EXPIRED_TIME = 24 * 60; // 캐시 만료 시간 (24시간)

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    // 직렬화 설정
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    return template;
  }

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(CACHE_EXPIRED_TIME))
        .disableCachingNullValues()
        .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
        .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

    return RedisCacheManager.builder(factory)
        .cacheDefaults(config)
        .build();
  }

  @Bean
  public RedissonClient redissonClient() {
    // Redisson의 Config 객체 생성
    Config config = new Config();
    String address = "redis://" + redisHost + ":" + redisPort;

    // Redis 단일 서버 설정
    config.useSingleServer()
        .setAddress(address) // Redis 서버 주소
        .setConnectionPoolSize(connectionPoolSize) // 연결 풀 크기
        .setConnectionMinimumIdleSize(connectionMinimumSize) // 최소 연결 수
        .setPassword(redisPassword); // 비밀번호 설정

    // RedissonClient 객체를 반환
    return Redisson.create(config);
  }
}