package com.cwh.springboot.springboot_aop.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

/** 日志记录
 * @author cwh
 * @date 2021/6/10 9:25
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
//  定义切入点
    @Pointcut("@annotation(com.cwh.springboot.springboot_aop.annotation.Log)")
    public void logPointCut(){}

//  定义Around通知，并标明匹配切入点
    @Around("logPointCut()")
//  Around通知必须要传入joinPoint，并且调用joinPoint.proceed()
    public Object recordTime(ProceedingJoinPoint joinPoint) throws Throwable {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        Long beginTime = System.currentTimeMillis();
        log.info(sdf.format(beginTime));
        log.info("开始执行");
        Object result = joinPoint.proceed();
        log.info("执行结束");
        Long endTime = System.currentTimeMillis();
        log.info(sdf.format(endTime));
        return result;
    }
}
