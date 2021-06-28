package com.cwh.springboot.async.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author cwh
 * @date 2021/6/28 17:16
 */
@Component
@Slf4j
public class GlobalAsyncExceptionHanlder implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("method:{},params:{} 发生异常",method,objects,throwable);
    }
}
