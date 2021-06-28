package com.cwh.springboot.async.service.impl;

import com.cwh.springboot.async.config.AsyncConfig;
import com.cwh.springboot.async.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

/**
 * @author cwh
 * @date 2021/6/25 11:26
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {


    @Override
    public Integer execute01()  {
        log.info("execute01");
        sleep(2);
        return 1;
    }

    @Override
    public Integer execute02()  {
        log.info("execute02");
        sleep(2);
        return 2;

    }

    @Async(AsyncConfig.EXECUTOR_ONE_NAME)
    @Override
    public void asyncExecute01() {
        this.execute01();
    }

    @Async(AsyncConfig.EXECUTOR_TWO_NAME)
    @Override
    public void asyncExecute02()  {
        this.execute02();
    }

    @Async
    @Override
    public Future<Integer> asyncExecuteWithValue01() {
        return AsyncResult.forValue(this.execute01());
    }

    @Async
    @Override
    public Future<Integer> asyncExecuteWithValue02() {
        return AsyncResult.forValue(this.execute02());

    }


    @Async
    public ListenableFuture<Integer>  asyncExecuteWithListenableFuture(){
        try{
            return AsyncResult.forValue(this.execute01());
        }catch(Throwable ex){
            return AsyncResult.forExecutionException(ex);
        }
    }

    @Async
    @Override
    public void testErrorHandler() {
        throw new RuntimeException("测试异常处理");
    }


    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
