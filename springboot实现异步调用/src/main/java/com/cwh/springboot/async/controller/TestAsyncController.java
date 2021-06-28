package com.cwh.springboot.async.controller;

import com.cwh.springboot.async.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author cwh
 * @date 2021/6/25 11:49
 */
@RestController
@RequestMapping("task")
@Slf4j
public class TestAsyncController {

    @Autowired
    private TaskService taskService;


    @GetMapping("/sync")
    public void syncTask() throws InterruptedException {
        log.info("=========同步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        taskService.execute01();
        taskService.execute02();

        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }

    @GetMapping("/async")
    public void asyncTask01() throws InterruptedException {
        log.info("=========同步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        taskService.asyncExecute01();
        taskService.asyncExecute02();

        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }

    @GetMapping("/async02")
    public void asyncTask02() throws InterruptedException, ExecutionException {
        log.info("=========同步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        Future<Integer> task01 = taskService.asyncExecuteWithValue01();
        Future<Integer> task02 = taskService.asyncExecuteWithValue02();

//        阻塞等待结果
        task01.get();
        task02.get();

        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }

    @GetMapping("/async03")
    public void asyncTask03() throws ExecutionException, InterruptedException {
        log.info("=========同步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        ListenableFuture<Integer> listenableFuture = taskService.asyncExecuteWithListenableFuture();
        listenableFuture.addCallback((result)->{log.info("========success&result={}=============",(result));},
                                    (ex)->{log.info("============failure&发生异常");});

//        添加listenableFutureCallback统一回调
        listenableFuture.addCallback(new ListenableFutureCallback<Integer>(){
            @Override
            public void onSuccess(Integer result) {
                log.info("success");
            }

            @Override
            public  void onFailure(Throwable ex){
                log.info("failure");
            }
        });
//        阻塞等待完成
        listenableFuture.get();

        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }


    @GetMapping("/error")
    public void testErrorHandler() throws InterruptedException {
        taskService.testErrorHandler();
        Thread.sleep(1000);
    }
}
