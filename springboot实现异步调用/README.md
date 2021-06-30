# Springboot实现异步调用

### 概述

我们之前的开发中往往都是直接按照逻辑写下去，不用考虑系统时间开销。这就是同步调用，即我的下一步操作必须等待上一步操作完成，但是，在一些场景下，我们希望使用异步调用而不是同步调用，即我让另外的资源去执行这个操作，而我的主逻辑不用等待这个操作的完成，这样毫无疑问节省了大量的时间。

* 同步调用：程序按照逻辑顺序依次执行，每一个操作都必须等待上一个操作完成后才能执行
* 异步调用：程序在执行过程中不必等待异步调用的完成即可完成之后的操作

很经典的例子就是去银行办理业务：

我今天计划要去银行办理一个业务，并且还打算去买个衣服，那么我先去银行，发现需要排队等待（即某个逻辑执行需要一段时间），那我取个号（开辟新的线程去执行），然后就直接去买衣服（继续执行主逻辑）。买完衣服后我再回来等待。



### java 多线程实现异步调用

对于异步调用，我们通常是为了可靠性使用消息队列中间件，例如之前提到的[`rabbitmq`](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88rabbitmq)，但是我们有的时候并不需要这么高的可靠性，为了减轻系统的复杂程度，我们使用多线程的方式实现异步调用。

> 这里可靠性是相对于线程的不可靠性来说的，因为线程数据是存储在内存中的，如果`JVM`进程被异常关闭那么线程信息将会直接丢失，而消息队列则是将消息数据存储在服务器上，即使`JVM`进程被异常关闭数据也不会丢失，这样可靠性大大增加。

```java
public class SyncTest {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        task(1);
        task(2);
        long end = System.currentTimeMillis();
        System.out.printf("花费时间为:%d ms",end-start);
    }

    static void task(Integer id){
        System.out.printf("==========task %d start======== \n",id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("==========task %d end========== \n",id);
    }
}
```

可以看到这里我们这里创建了两个`task`，采用同步的方式运行，那么`task2`必然就会等到`task1`完成才会开始执行

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629101211.png)

接下来我们采用异步操作方式运行

```java
public class AsyncTest {

    public static void main(String[] args) throws InterruptedException {
//        创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(10);
//        设置终止计数
        CountDownLatch countDownLatch = new CountDownLatch(2);
        long start = System.currentTimeMillis();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                task(1);
                countDownLatch.countDown();
            }
        });

        executor.submit(new Runnable() {
            @Override
            public void run() {
                task(2);
                countDownLatch.countDown();
            }
        });
//		  等待所有任务完成
        countDownLatch.await();
        long end = System.currentTimeMillis();
        System.out.printf("花费时间为:%d ms",end-start);


    }

   static void task(Integer id){
        System.out.printf("==========task %d start======== \n",id);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("==========task %d end========== \n",id);
    }
}

```

这里我们通过线程池新建两个任务，任务内容与同步任务相同，但是是异步执行

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629102034.png)

可以看到花费时间大大减少



### springboot实现异步调用

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot%E5%AE%9E%E7%8E%B0%E5%BC%82%E6%AD%A5%E8%B0%83%E7%94%A8)

#### 异步调用

`spring framework`为我们提供了`@Async`注解，通过将该注解添加在方法上，以`AOP`的方式自动实现方法的异步调用。

> 原理是基于`aop`拦截，将异步操作提交到线程池中以实现异步调用的操作



对于`springboot `中异步调用，我们不需要添加额外的依赖，只需要导入`spring boot`依赖即可

##### pom.xml

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
```

并且在启动类上添加`@EnableAsync`开启`@Async`即可



##### ServiceImpl

我们定义两个非异步操作以及两个异步操作来测试是否实现异步调用

```java
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    
    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

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

    @Async
    @Override
    public void asyncExecute01() {
        this.execute01();
    }

    @Async
    @Override
    public void asyncExecute02()  {
        this.execute02();
    }
```

> 使用`@Async`标明该方法为异步调用，会将该方法提交给线程池创建线程

> 这里使用`sleep()`模拟方法耗时



##### 同步测试

```java
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

```

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629103939.png)

可以看到没有添加`@Async`的方法依照顺序执行

##### 异步测试

```java
    @GetMapping("/async")
    public void asyncTask01() throws InterruptedException {
        log.info("=========异步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        taskService.asyncExecute01();
        taskService.asyncExecute02();

        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }
```



![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629104623.png)

可以看到提交任务之后我们主逻辑并没有等待两个异步任务的完成，即实现了异步调用



##### AsyncResult

可以看到我们上面并没有等待异步线程即执行了主逻辑，但是有的时候我们又确实需要等待线程的完成或者需要线程的返回结果

这里我们就要使用`Future`接口

`Future`接口用来接收多线程的执行结果，类似于一个代理，我们可以通过`Future`获取线程任务的状态以及返回结果。

> `boolean isDone()` ：返回任务是否已经完成
>
> `V get()` ：获得任务返回的值
>
> `boolean cancel()`：取消任务
>
> ...



`AsyncResult`实现了`ListenableFuture`，而`ListenableFuture`继承`Future`

> `ListenableFuture` 新增了回调函数和转化为`completableFuture`的方法
>
> 关于回调函数和`completableFuture`后面会介绍，这里先介绍`Future`功能

我们通过静态方法构建`Future`对象

```java
    public static <V> ListenableFuture<V> forValue(V value) {
        return new AsyncResult(value, (Throwable)null);
    }
```

```java
    private AsyncResult(@Nullable V value, @Nullable Throwable ex) {
        this.value = value;
        this.executionException = ex;
    }
```



所以我们可以通过`AsyncResult.forvalue(task)`构建`Future`并使用`get()`方法等待线程完成

```java
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
```

```java
    @GetMapping("/async02")
    public void asyncTask02() throws InterruptedException, ExecutionException {
        log.info("=========异步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        Future<Integer> task01 = taskService.asyncExecuteWithValue01();
        Future<Integer> task02 = taskService.asyncExecuteWithValue02();

//        阻塞等待结果
        log.info(task01.get().toString());
        log.info(task02.get().toString());


        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }
```

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629112456.png)

可以看到主程序等待线程任务完成并且可以通过`Future.get()`获取返回值



#### 异步回调

通过`get()`方法等待线程完成实际上还是堵塞操作，还是会耗费一些不必要的时间，是一种比较老旧的不科学的方法，所以`ListenableFuture`新增了`addCallback()`回调函数

##### 回调函数

回调函数，顾名思义，就是当触发某个条件时调用某个函数。最常用的就是成功回调和异常回调，即我线程成功完成之后调用的函数和线程执行时抛出异常调用的函数

```java
public interface ListenableFuture<T> extends Future<T> {
    void addCallback(ListenableFutureCallback<? super T> var1);

    void addCallback(SuccessCallback<? super T> var1, FailureCallback var2);

    default CompletableFuture<T> completable() {
        CompletableFuture<T> completable = new DelegatingCompletableFuture(this);
        this.addCallback(completable::complete, completable::completeExceptionally);
        return completable;
    }
}
```

这是`ListenableFuture`的接口源码：

`addCallback(ListenableCallback)`：这里`ListenableCallback`为统一回调函数接口，继承了`SuccessCallback`和`FailureCallback`

`addCallback(SuccessCallback,FailureCallback)`：添加成功回调和失败回调

`completable()`：对`ListenableFuture`封装成`CompletableFuture`



所以我们可以通过`addCallback`添加我们自定义的回调函数

* 分别添加成功回调和失败回调

  ```java
  //        添加成功回调和失败回调
          listenableFuture.addCallback(
              (result)->{log.info("========success&result={}=============",(result));},
                                      
              (ex)->{log.info("============failure&发生异常");});
  
  ```

* 添加统一回调

  ```java
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
  ```

>  在调用`@Async`的方法时，如果返回的是`ListenableFuture`，则实际返回的是`ListenableFutureTask`对象，从而调用`ListenableFutureTask`中定义的`addCallback()`
>
> 这个我也不清楚，网上看到有人这么说，但是源码里面没看到啥，也搜不到啥，之后有空再仔细看看

##### 实例展示

```java
    @Async(AsyncConfig.EXECUTOR_ONE_NAME)
    public ListenableFuture<Integer>  asyncExecuteWithListenableFuture(){
        try{
            return AsyncResult.forValue(this.execute01());
        }catch(Throwable ex){
            return AsyncResult.forExecutionException(ex);
        }
    }
```

这里我们定义一个异步操作，返回一个`ListenableFuture`对象

我们对其添加回调函数

```java
    @GetMapping("/async03")
    public void asyncTask03() throws ExecutionException, InterruptedException {
        log.info("=========异步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        ListenableFuture<Integer> listenableFuture = taskService.asyncExecuteWithListenableFuture();
//        添加成功回调和失败回调
        listenableFuture.addCallback(
            (result)->{log.info("========success&result={}=============",(result));},                       
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
```

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629173803.png)

可以看到当任务完成是成功调用了回调函数



#### 异步异常处理器

##### GlobalAsyncExceptionHandler

在之前的项目中，我们有实现过对于异常的统一处理，这里我们也可以设置对于异步异常的统一处理

```java
@Component
@Slf4j
public class GlobalAsyncExceptionHanlder implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("method:{},params:{} 发生异常",method,objects,throwable);
    }
}
```

这里们实现`AsyncUncaughtExceptionHandler`并覆写`handleUncaughtException`即异常处理器

我们需要注意，异常处理器只能拦截返回类型为非`Future`的异步调用方法

> 源码没看那么懂，感觉貌似是错的，有空再修改

```java
// 拦截器核心方法源码   
@Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Class<?> targetClass = 
            invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null;
        Method specificMethod =
            ClassUtils.getMostSpecificMethod(invocation.getMethod(), targetClass);
        Method userDeclaredMethod = 
            BridgeMethodResolver.findBridgedMethod(specificMethod);
        //选择执行器
        AsyncTaskExecutor executor = this.determineAsyncExecutor(userDeclaredMethod);
        if (executor == null) {
            throw new IllegalStateException("No executor specified and no default executor set on AsyncExecutionInterceptor either");
        } else {
            Callable<Object> task = () -> {
                try {
                    //执行异步任务
                    Object result = invocation.proceed();
                    //如果是future类则不使用handler进行处理
                    if (result instanceof Future) {
                        return ((Future)result).get();
                    }
                } catch (ExecutionException var4) {
                    this.handleError(var4.getCause(), userDeclaredMethod, invocation.getArguments());
                } catch (Throwable var5) {
                    this.handleError(var5, userDeclaredMethod, invocation.getArguments());
                }

                return null;
            };
            return this.doSubmit(task, executor, invocation.getMethod().getReturnType());
        }
    }
```



##### AsyncConfig

定义了异步异常处理器之后，我们接下来就需要将其配置到我们异常模块中

```java
@Configuration
public class AsyncConfig  implements AsyncConfigurer {
    @Autowired
    private GlobalAsyncExceptionHanlder globalAsyncExceptionHanlder;
    
     @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return globalAsyncExceptionHanlder;
    }
}
```

实现`AsyncConfigurer`接口，实现异步相关的配置操作

`getAsyncUncaughtExceptionHandler()`：配置异常处理器



##### 实例展示

我们定义一个会抛出异常的异步操作

```java
    @Async
    @Override
    public void testErrorHandler() {
        throw new RuntimeException("测试异常处理");
    }
```

调用该操作

```java
    @GetMapping("/error")
    public void testErrorHandler() throws InterruptedException {
        taskService.testErrorHandler();
        Thread.sleep(1000);
    }
```

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210629180224.png" style="zoom:150%;" />

可以看到这里按照我们配置的异常处理器中的设置对异常进行处理



#### 自定义执行器

##### Executor，ExecutorService，Executors

我们之前对于线程的创建都是`Thread.start()`，手动创建线程，不仅是每次新建对象性能差，缺乏对线程的统一管理也没有对线程的一些其他操作，例如定时执行等等。

所以我们使用`Executor`框架，使用线程池对线程进行创建和统一管理

> 线程池的优点：
>
> 1. 避免重复创建和销毁线程造成资源的浪费
>
> 2. 提高响应速度，因为不用每次都新建线程
>
> 3. 提高线程的可管理性，可以使用线程池进行统一管理和调优



* `Executor`：`Executor`框架的父类接口，定义了一个能够接受`Runnable`对象的`executor`方法`execute()`，用来执行任务，开辟新线程

* `ExecutorService`：`Executor`的子接口，新增了对线程的生命周期的追踪管理。

  提供`submit()`方法：接受`Callable`或者`Runnale`对象，返回一个`Future`对象，通过对`future`对象的`get()`或者`isDone()`等操作追踪任务状况，并且可以通过`shutdown`平滑的关闭线程池

  ![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210630100132.png)

* `Executors` ：提供了一系列的工厂方法用于创建线程池，返回的线程池都实现了`ExecutorService`：

  1. `newCacheThreadPool()`：
     * 缓存型池子，先查看池中有没有以前建立的线程，如果有，就 reuse.如果没有，就建一个新的线程加入池中
     * 缓存型池子通常用于执行一些**生存期很短的异步型任务**
        因此在一些面向连接的daemon型SERVER中用得不多。但对于生存期短的异步任务，它是Executor的首选。
     * 能reuse的线程，必须是timeout IDLE内的池中线程，**缺省   timeout是60s**,超过这个IDLE时长，线程实例将被终止及移出池。
        注意，放入CachedThreadPool的线程不必担心其结束，**超过TIMEOUT不活动，其会自动被终止。**
  2. `newFixedThreadPool()`：
     * 也能`reuse`，但是不能随时创建新的线程
     * 其独特之处:任意时间点，最多只能有固定数目的活动线程存在，**此时如果有新的线程要建立，只能放在另外的队列中等待**，**直到当前的线程中某个线程终止直接被移出池子**
     * 和cacheThreadPool不同，FixedThreadPool没有IDLE机制（可能也有，但既然文档没提，肯定非常长，类似依赖上层的TCP或UDP IDLE机制之类的），所以**FixedThreadPool多数针对一些很稳定很固定的正规并发线程，多用于服务器**
  3. `newScheduledThreadPool()`
     * 调度型线程池
     * 这个池子里的线程可以按schedule依次delay执行，或周期执行
  4. `singleThreadExecutor()`
     * 单例线程，任意时间池中只能有一个线程

  

##### ThreadPoolTaskExecutor

非`Spring`项目中，我们常用`Executors.newXXThreadPool()`或者`new ThreadPoolExecutor`构建线程池，但是`spring`为我们封装了新的线程池类`ThreadPoolTaskExecutor`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210630102539.png" style="zoom: 80%;" />

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210630102600.png" style="zoom:80%;" />

可以看到`ThreadPoolTaskExecutor`和`ThreadPoolExecutor`祖类都继承了`Executor`接口，但是`ThreadPoolTaskExecutor`实现了`InitializingBean`，`DisposableBean`，`XXAware`等，提供了`spring`生命周期和参数装配



##### application.yml配置

```yaml
spring:
  task:
    execution-one:
#     线程池中线程前缀
      thread-name-prefix: threadpool01-
#     线程池相关属性
      pool:
        core-size: 8 #核心线程数，线程池创建时初始化的线程数，默认为8
        max-size: 20 #最大线程数，当缓存队列满了之后才会申请超过核心线程数的线程
        keep-alive: 60s #允许线程的空闲时间，非核心线程的线程达到时间后会被销毁，默认为60s
        queue-capacity: 200 #缓冲队列大小，用来缓冲执行任务队列的大小
        allow-core-thread-timeout: true #是否允许核心线程超时，即开启线程池的动态增长和减小，默认为true
#     线程池关闭设置
    shutdown:
      await-termination: true #应用关闭时，是否等待定时任务执行完成
      await-termination-period: 60 #等待任务完成的最大时长

    execution-two:
      thread-name-prefix: threadpool02-
      pool:
        core-size: 8
        max-size: 20
        keep-alive: 60s
        queue-capacity: 200
        allow-core-thread-timeout: true
      shutdown:
        await-termination: true
        await-termination-period: 60
```

这里我们通过配置文件设置了两个线程池的参数，相关参数见注解

之后使用`TaskExecutionProperties`属性配置类来映射



##### AsyncConfig

```java
@Configuration
public class AsyncConfig  implements AsyncConfigurer {

    public static final String EXECUTOR_ONE_NAME = "executor01";
    public static final String EXECUTOR_TWO_NAME = "executor02";


    @Autowired
    private GlobalAsyncExceptionHanlder globalAsyncExceptionHanlder;

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return globalAsyncExceptionHanlder;
    }

    @Configuration
    public static class ExecutorOneConfiguration{

        //配置映射类映射线程池属性
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
```

我们这里使用`TaskExecutionProperties`将`application.yml`中配置的线程池的属性映射进来，再通过`TaskExecutorBuilder`构建线程池



##### 实例展示

```java
    @Async(AsyncConfig.EXECUTOR_ONE_NAME)
    @Override
    public Future<Integer> asyncExecuteWithValue01() {
        return AsyncResult.forValue(this.execute01());
    }

    @Async(AsyncConfig.EXECUTOR_TWO_NAME)
    @Override
    public Future<Integer> asyncExecuteWithValue02() {
        return AsyncResult.forValue(this.execute02());
    }

```

这里通过`@Async()`设置使用线程池对应`BeanName`

```java
    @GetMapping("/async02")
    public void asyncTask02() throws InterruptedException, ExecutionException {
        log.info("=========异步任务===========");
        long start = System.currentTimeMillis();
        log.info("=========开始执行===========");

        Future<Integer> task01 = taskService.asyncExecuteWithValue01();
        Future<Integer> task02 = taskService.asyncExecuteWithValue02();

//        阻塞等待结果
        log.info(task01.get().toString());
        log.info(task02.get().toString());


        long end = System.currentTimeMillis();
        log.info("=========执行完毕===========");
        log.info("=========花费时间：{}ms",end-start);
    }
```

再次调用`asyncTask02()`，不做任何修改

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210630105121.png)

可以看到线程中有了我们定义的前缀，说明使用的是我们定义的线程池



### 总结

对于一些可靠性要求不是很高的功能，我们可以使用多线程的方法实现异步调用，可靠性要求高的功能还是建议使用消息队列作为中间件。

此外，线程池的大小设置也非常有考究，设置过大造成资源浪费，设置过小易造成阻塞，对于线程池优化还需要细看。
