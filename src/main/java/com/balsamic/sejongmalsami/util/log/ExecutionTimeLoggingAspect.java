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

  @Around("@annotation(LogTimeInvocation)|| @annotation(LogMonitoringInvocation)")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Object result = null;

    long startTime = System.currentTimeMillis();
    try {
      result = joinPoint.proceed();
    } finally {
      long endTime = System.currentTimeMillis();
      long durationTimeSec = endTime - startTime;
      log.info("[{}] 실행시간: {}ms", signature.getMethod().getName(), durationTimeSec);
    }

    return result;
  }
}