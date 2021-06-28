package com.cwh.springboot.async.service;

import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;

/**
 * @author cwh
 * @date 2021/6/25 11:21
 */
public interface TaskService {

    public Integer execute01() throws InterruptedException;

    public Integer execute02() throws InterruptedException;

    public void asyncExecute01() throws InterruptedException;

    public void asyncExecute02() throws InterruptedException;

    public Future<Integer> asyncExecuteWithValue01();

    public Future<Integer> asyncExecuteWithValue02();

    public ListenableFuture<Integer> asyncExecuteWithListenableFuture();

    public void testErrorHandler();

}
