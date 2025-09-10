package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AfterLoggingAspect {

    // 1️⃣ @After → runs ALWAYS after method (success or failure)
    @After("execution(* com.example.demo.service.OrderService.getAllOrders(..))")
    public void afterFinallyExample(JoinPoint joinPoint) {
        log.info("[AFTER] Method finished (success or fail): {}", joinPoint.getSignature());
    }

    // 2️⃣ @AfterReturning → only when method RETURNS successfully
    @AfterReturning(
            pointcut = "execution(* com.example.demo.service.OrderService.getOrderById(..))",
            returning = "result")
    public void afterReturningExample(JoinPoint joinPoint, Object result) {
        log.info("[AFTER RETURNING] {} returned value: {}",
                 joinPoint.getSignature(), result);
    }

    // 3️⃣ @AfterThrowing → only when method THROWS an exception
    @AfterThrowing(
            pointcut = "execution(* com.example.demo.service.OrderService.createOrder(..))",
            throwing = "ex")
    public void afterThrowingExample(JoinPoint joinPoint, Throwable ex) {
        log.error("[AFTER THROWING] Exception in {} | Message: {}",
                  joinPoint.getSignature(), ex.getMessage(), ex);
    }
}
