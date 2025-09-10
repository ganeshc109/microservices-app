package com.example.demo.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // Corrected: Added .* for method name
    @Before("execution(* com.example.demo..service..*.*(..))")
    public void logBeforeService(JoinPoint joinPoint) {
        log.info("[EXECUTION] Method called in Service: {}", joinPoint.getSignature());
    }

    // Corrected: Added .* for method name (within is fine but combined for clarity)
    @Before("within(com.example.demo..controller..*)")
    public void logWithinControllers(JoinPoint joinPoint) {
        log.info("[WITHIN] Controller method: {}", joinPoint.getSignature());
    }

    @After("@annotation(com.example.demo.aspect.TrackExecution)")
    public void logAfterAnnotatedMethod(JoinPoint joinPoint) {
        log.info("[@ANNOTATION] Tracked execution: {}", joinPoint.getSignature());
    }

//     @Before("execution(* com.example.demo..*(..)) && args(com.example.demo.dto.OrderEvent,..)")
// public void logOrderEventArgs(JoinPoint joinPoint) {
//     log.info("[ARGS] Method {} called with OrderEvent param", joinPoint.getSignature());
// }


    @Before("bean(*Service) && within(com.example.demo..*)")
    public void logBeanServices(JoinPoint joinPoint) {
        log.info("[BEAN] Service bean method: {}", joinPoint.getSignature());
    }

    // Corrected: Added .* for method name in both parts
    @Around("execution(* com.example.demo..service..*.*(..)) || execution(* com.example.demo..controller..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        log.debug("🔎 [DEBUG] Intercepting bean: {} | Method: {}",
                joinPoint.getTarget().getClass().getName(),
                joinPoint.getSignature());
        log.info("➡️ Entering method: {}", joinPoint.getSignature());
        try {
            Object result = joinPoint.proceed();
            long timeTaken = System.currentTimeMillis() - startTime;
            log.info("✅ Exiting method: {} | Execution time = {} ms",
                    joinPoint.getSignature(), timeTaken);
            return result;
        } catch (Throwable ex) {
            long timeTaken = System.currentTimeMillis() - startTime;
            log.error("❌ Exception in method: {} | Time until failure = {} ms | Message: {}",
                    joinPoint.getSignature(), timeTaken, ex.getMessage(), ex);
            throw ex;
        }
    }
}
