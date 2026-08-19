package de.fi.webapp.aspect;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

    @Pointcut("execution(public * de.fi.webapp.presentation.controller.v1.PersonenController.*(..))")
    public void PersonControllerMethodes() {}

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceMethods(){}

    @Pointcut("@within(de.fi.webapp.aspect.Benchmark)")
    public void benchmark() {
    }
}
