package de.fi.webapp.aspect;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

@Component
@Aspect
@Slf4j
public class MyAspect {

    //private static Logger logger = Logger.getLogger(MyAspect.class.getName());




    @Before("Pointcuts.PersonControllerMethodes()")
    public void beforeAdvice(JoinPoint joinPoint) {
        log.warn(String.format("######## beforeAdvice Methode  %s wurde gerufen!  #########", joinPoint.getSignature().getName()));
    }
    @AfterReturning(value = "Pointcuts.PersonControllerMethodes()", returning ="result")
    public void afterReturningAdvice(JoinPoint joinPoint, Object result) {
        log.warn((String.format("############################# Afterreturning: %s ######################", joinPoint.getSignature().getName())));
        log.warn(String.format("############################# Result: %s ######################", result.toString()));
    }

    @AfterThrowing(value="execution(public * de.fi.webapp.presentation.controller.v1.PersonenController.*(..))", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.warn(String.format("############################# Afterreturning: %s ######################", joinPoint.getSignature().getName()));
        log.warn(String.format("############################# Exception: %s ######################", ex.toString()));
    }

    @After(value="execution(public * de.fi.webapp.presentation.controller.v1.PersonenController.*(..))")
    public void after(JoinPoint joinPoint) {
        log.warn(String.format("############################# After: %s ######################", joinPoint.getSignature().getName()));

    }
    @Around("Pointcuts.benchmark()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant start = Instant.now();
        Object result = joinPoint.proceed();
        Instant end = Instant.now();
        var duration = Duration.between(start, end).toMillis();
        System.out.println("############################# Duration: " + duration);
        return result;
    }
}
