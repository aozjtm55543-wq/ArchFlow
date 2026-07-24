package com.archflow.aop;

import com.archflow.annotation.AiPerformanceTrace;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AiPerformanceAspect {
    private static final Logger log = LoggerFactory.getLogger(AiPerformanceAspect.class);
    private static final long SLOW_THRESHOLD_MS = 8000L;

    @Around("@annotation(aiPerformanceTrace)")
    public Object trace(ProceedingJoinPoint joinPoint, AiPerformanceTrace aiPerformanceTrace) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("AI performance | method={} | elapsedMs={}", methodName, elapsed);
            if (elapsed > SLOW_THRESHOLD_MS) {
                log.warn("Slow AI call detected | method={} | elapsedMs={}", methodName, elapsed);
            }
            return result;
        } catch (Throwable exception) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("AI performance failed | method={} | elapsedMs={}", methodName, elapsed);
            throw exception;
        }
    }
}
