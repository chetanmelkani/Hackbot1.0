package com.hackbot.utility;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Arrays;

/**
 * Created by chetanmelkani on 10/07/15.
 */
@Aspect
public class TraceAspect {

    @Pointcut("within(com.hackbot.*)")
    public void allMethodsPointcut(){}

    @Around("allMethodsPointcut()")
    public Object callDurationAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("This beeing callede3");
        Signature signature = pjp.getSignature();
        Object[] args = pjp.getArgs();
        String argList = Arrays.toString(args);
        System.out.println(signature.getDeclaringTypeName() +
                "." + signature.getName() + "(" + argList + ") started");
        long s = System.nanoTime();
        Object proceed = pjp.proceed(args);
        long e = System.nanoTime();
        System.out.println(signature.getDeclaringTypeName() +
                "." + signature.getName() + "(" + argList + ") ended after " +
                ((double)(e-s)/1000000) + " ms");
        return proceed;
    }

    @Before("allMethodsPointcut()")
    public void allServiceMethodsAdvice(){
        System.out.println("Before executing service method");
    }

}
