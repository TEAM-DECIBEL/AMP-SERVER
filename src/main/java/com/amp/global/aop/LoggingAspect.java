package com.amp.global.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // controller 메서드 실행 전후 로깅
    @Around("execution(* com.amp.domain..controller.*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String httpMethod = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";

        log.info("======= [REQUEST] {} {} =======", httpMethod, uri);
        log.debug("[Controller] {}.{} 시작", className, methodName);
        log.info("[Parameters]: {}", Arrays.toString(joinPoint.getArgs()));

        Object result = null;
        try {
            result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("======= [Response: {}] =======", result);
            log.debug("[Controller] {}.{} 완료 ({}ms)", className, methodName, executionTime);

            return result;
        } catch (Exception e) {

            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[Controller - ERROR] {}.{} 실패 ({}ms) - {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }

    // Service 메서드 실행 전후 로깅
    @Around("execution(* com.amp.domain..application.*.*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("[Service] {}.{} 시작", className, methodName);

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("[Service] {}.{} 완료 ({}ms)", className, methodName, executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[Service - ERROR] {}.{} 실패 ({}ms) - {}",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}
