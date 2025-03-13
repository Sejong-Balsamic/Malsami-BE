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

  /**
   * RequestContextHolder를 통해 현재 HTTP 요청 객체를 가져와 RequestID 값을 읽음
   */
  @Around("@annotation(LogMethodInvocation) || @annotation(LogMonitoringInvocation)")
  public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();

    // 현재 HTTP 요청 객체를 얻음
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    HttpServletRequest request = attributes.getRequest();

    // RequestID 속성을 읽음
    String requestId = (String) request.getAttribute("RequestID");

    // 메서드 호출 전 로그 기록
    LOGGER.debug("[{}] RequestID: {}, Parameter: {}",
        signature.getMethod().getName(),
        requestId,
        Arrays.toString(joinPoint.getArgs()));

    Object result = ErrorCode.INTERNAL_SERVER_ERROR; // 기본 반환값 설정
    try {
      result = joinPoint.proceed(); // 메서드 실행
    } finally {
      // 메서드 호출 후 결과 값 로그 기록
      LOGGER.debug("[{}] RequestID: {}, Result: {}",
          signature.getMethod().getName(),
          requestId,
          result);
    }

    return result;
  }
}
