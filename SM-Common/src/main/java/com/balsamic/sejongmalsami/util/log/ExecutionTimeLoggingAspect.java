package com.balsamic.sejongmalsami.util.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionTimeLoggingAspect {

  /**
   * LogTimeInvocation, LogMonitoringInvocation 어노테이션이 붙은 메서드가 호출될 때 해당 어드바이스 동작
   * ProceedingJoinPoint 를 통해 호출 대상(메서드) 정보 가져옴
   */
  @Around("@annotation(LogTimeInvocation) || @annotation(LogMonitoringInvocation)")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Object result;

    long startTime = System.currentTimeMillis(); // 메서드 시작시간 (ms)
    try {
      result = joinPoint.proceed(); // 메서드 실행
    } finally {
      long endTime = System.currentTimeMillis(); // 메서드 종료시간 (ms)
      long durationTimeSec = endTime - startTime;
      log.debug("[{}] 실행시간: {}ms", signature.getMethod().getName(), durationTimeSec);
    }

    return result;
  }
}
