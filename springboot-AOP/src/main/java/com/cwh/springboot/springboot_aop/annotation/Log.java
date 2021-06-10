package com.cwh.springboot.springboot_aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，以标记切入点
 *
 * @author cwh
 * @date 2021/6/10 9:25
 */
//设置注解位置，此处为方法上注解
@Target(ElementType.METHOD)
//设置注解生命周期，此处为存在程序运行过程中
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String value() default "";
}
