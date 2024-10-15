package com.balsamic.sejongmalsami.util.log;

import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MethodInvocationLoggingAspect {

  private final Logger LOGGER = LoggerFactory.getLogger(MethodInvocationLoggingAspect.class);

  @Around("@annotation(LogMethodInvocation) || @annotation(LogMonitoringInvocation)")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = attributes.getRequest();
    String requestId = (String) request.getAttribute("RequestID");

    LOGGER.info("[{}] RequestID: {}, Parameter: {}", signature.getMethod().getName(), requestId,
        Arrays.toString(joinPoint.getArgs()));

    Object result = ErrorCode.INTERNAL_SERVER_ERROR;
    try {
      result = joinPoint.proceed();
    } finally {
      LOGGER.info("[{}] RequestID: {}, Result: {}", signature.getMethod().getName(), requestId, result);
    }

    return result;
  }
}
