package com.balsamic.sejongmalsami.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisLockManager {

  @Value("${spring.data.redis.wait-time}")
  private Long waitTime;

  @Value("${spring.data.redis.lease-time}")
  private Long leaseTime;

  private final RedissonClient redissonClient;

  /**
   * 락을 실행하며, 작업이 완료되면 락을 자동으로 해제합니다.
   *
   * @param lockKey 키값
   * @param task 실행할 작업
   * @param <T> 작업 실행 결과 타입
   * @return 작업 실행 결과
   */
  public <T> T executeLock(String lockKey, LockTask<T> task) {
    RLock lock = redissonClient.getLock(lockKey);
    // 락 획득 시도
    try {
      // 최대 waitTime초간 대기, 락 획득 후 leaseTime 뒤 자동 만료 설정
      if (!lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) { // 락 획득 실패시
        log.error("락 획득 실패 - 다른 요청 처리 중. lockKey: {}", lockKey);
        throw new IllegalStateException("락 획득 실패");
      }

      try {
        // 락 획득 성공 시 작업 진행
        return task.run();
      } catch (Exception e) {
        log.error("작업 실행 중 예외 발생", e);
        throw new RuntimeException("작업 실행 중 예외 발생", e);
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("락 획득 대기 중 인터럽트 발생", e);
      throw new IllegalStateException("락 대기 중 인터럽트 발생", e);
    } finally { // 현재 스레드가 락을 가지고 있는 경우
      if (lock.isHeldByCurrentThread()) {
        log.info("현재 스레드가 락을 가지고 있어 해제합니다.");
        lock.unlock();
      }
    }
  }

  @FunctionalInterface
  public interface LockTask<T> {
    T run() throws Exception;
  }

}
