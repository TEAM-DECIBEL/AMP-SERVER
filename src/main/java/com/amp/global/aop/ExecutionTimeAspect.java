package com.amp.global.aop;

import com.amp.global.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {
    @Around("@annotation(com.amp.global.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LogExecutionTime annotation = signature.getMethod().getAnnotation(LogExecutionTime.class);

        String taskName = annotation.value().isEmpty()
                ? signature.getName()
                : annotation.value();

        long startTime = System.currentTimeMillis();

        log.info("[{}] 시작", taskName);

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("[{}] 성공 - 실행 시간: {}ms", taskName, executionTime);

            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("[{}] 실패 - 실행 시간: {}ms, 예외: {}",
                    taskName, executionTime, e.getMessage());
            throw e;
        }
    }
}
