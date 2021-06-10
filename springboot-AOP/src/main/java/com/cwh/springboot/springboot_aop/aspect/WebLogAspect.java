package com.cwh.springboot.springboot_aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author cwh
 * @date 2021/6/10 9:25
 */
@Aspect
@Component
@Slf4j
public class WebLogAspect {
//  定义切入点
    @Pointcut("execution(* com.cwh.springboot.springboot_aop.controller.*.*(..))")
    public void logPointCut(){}

//  前置通知 获取Request参数
    @Before("logPointCut()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        log.info("请求地址:"+request.getRequestURI().toString());
        log.info("HTTP METHOD:"+request.getMethod());

        log.info("CLASS_METHOD:"+joinPoint.getSignature().getDeclaringTypeName()+"."+
                joinPoint.getSignature().getName());

        log.info("参数:"+ Arrays.toString(joinPoint.getArgs()));

    }

//  后置返回通知 获取返回值
    @AfterReturning(returning = "result", pointcut = "logPointCut()")
    public void doAfterReturning(Object result) throws Throwable{
        log.info("返回值为："+result);
    }

}
