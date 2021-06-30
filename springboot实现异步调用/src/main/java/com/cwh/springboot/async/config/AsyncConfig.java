package com.cwh.springboot.async.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author cwh
 * @date 2021/6/28 17:20
 */
@Configuration
public class AsyncConfig  implements AsyncConfigurer {

    public static final String EXECUTOR_ONE_NAME = "executor01";
    public static final String EXECUTOR_TWO_NAME = "executor02";


    @Autowired
    private GlobalAsyncExceptionHanlder globalAsyncExceptionHanlder;

//    @Override
//    public Executor getAsyncExecutor(){
//        return null;
//    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return globalAsyncExceptionHanlder;
    }

    @Configuration
    public static class ExecutorOneConfiguration{

        @Bean(name = EXECUTOR_ONE_NAME + "-properties")
        @Primary
        @ConfigurationProperties(prefix= "spring.task.execution-one")
        public TaskExecutionProperties taskExecutionProperties(){
            return new TaskExecutionProperties();
        }

        @Bean(name = EXECUTOR_ONE_NAME)
        public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
            TaskExecutorBuilder builder = createTskExecutorBuilder(this.taskExecutionProperties());
            return  builder.build();
        }
    }

    @Configuration
    public static class ExecutorTwoConfiguration{

        @Bean(name = EXECUTOR_TWO_NAME + "-properties")
        @ConfigurationProperties(prefix= "spring.task.execution-two")
        public TaskExecutionProperties taskExecutionProperties(){
            return new TaskExecutionProperties();
        }

        @Bean(name = EXECUTOR_TWO_NAME)
        public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
            TaskExecutorBuilder builder = createTskExecutorBuilder(this.taskExecutionProperties());
            return  builder.build();
        }
    }

    private static TaskExecutorBuilder createTskExecutorBuilder(TaskExecutionProperties properties){
//        pool属性设置
        TaskExecutionProperties.Pool pool = properties.getPool();
        TaskExecutorBuilder builder = new TaskExecutorBuilder();
        builder = builder.queueCapacity(pool.getQueueCapacity());
        builder = builder.corePoolSize(pool.getCoreSize());
        builder = builder.maxPoolSize(pool.getMaxSize());
        builder = builder.allowCoreThreadTimeOut(pool.isAllowCoreThreadTimeout());
        builder = builder.keepAlive(pool.getKeepAlive());
        // Shutdown 属性
        TaskExecutionProperties.Shutdown shutdown = properties.getShutdown();
        builder = builder.awaitTermination(shutdown.isAwaitTermination());
        builder = builder.awaitTerminationPeriod(shutdown.getAwaitTerminationPeriod());
        // 其它基本属性
        builder = builder.threadNamePrefix(properties.getThreadNamePrefix());

        return builder;
    }


}
