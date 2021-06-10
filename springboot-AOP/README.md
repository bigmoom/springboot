# SpringBoot 使用 AOP

### AOP 简介

`Springboot`两个核心思想便是`IOC`和`AOP`。关于`IOC`控制反转我在[初识SpringBoot]()中已经具体介绍了，所以这次就来仔细介绍一下`AOP`

`AOP`即`Aspect Oriented Programming`，面向切面编程。`AOP`和`OOP`一样，只是一种编程范式，没有具体的编程规范。

我们先来看一个简单的例子：

我们有三个类，我想在对每个类都添加日志记录的功能

```java
Public class A{
 Public void do(){
 …
 Record.addRecord();//添加日志
 }
}

Public class B{
 Public void do(){
 …
 Record.addRecord();//添加日志
 }
}
Public class C{
 Public void do(){
 …
 Record.addRecord();//添加日志
 }
}
```

这就是我们之前`OOP`思想下的实现方法，虽然没有什么问题，但是很繁琐。毕竟我们的项目不可能就只有几个类，我们的功能也不可能只有记录日志这种功能，倘若要一个一个手动添加编写实在是太过低效。

此时`AOP`就思想就很重要。

简单来说，就是横向看我们的程序，有很多方法并列，此时我们对某一类方法一刀切，设置一个处理功能，例如记录日志，权限认证，异常处理等等。

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210610103319.png)

在`Springboot`中，我们就是设置一个切入点标识，标记一类方法。然后我们编写切入点要实现的功能。这样程序运行时，捕获到切入点即调用切入点的方法进行相应处理。

```java
@Aspect
@Component
@Slf4j
public class LogAspect {

    @Pointcut("@annotation(com.cwh.springboot.springboot_aop.annotation.Log)")
    public void logPointCut(){}

    @Around("logPointCut()")
    public Object recordTime(ProceedingJoinPoint joint) throws Throwable {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        Long beginTime = System.currentTimeMillis();
        log.info(sdf.format(beginTime));
        log.info("开始执行");
        Object result = joint.proceed();
        log.info("执行结束");
        Long endTime = System.currentTimeMillis();
        log.info(sdf.format(endTime));
        return result;
    }
}
```

例如这个简单的日志记录的切面

` @Pointcut("@annotation(com.cwh.springboot.springboot_aop.annotation.Log)")`这是标明使用了放在`com.cwh.springboot.springboot_aop.annotation`中的`Log`注解的地方为切入点

`@Around("logPointCut()")`这是标明我们在上述定义的切入点要执行的操作，具体介绍后面会细讲。

`SpringBoot`中的`AOP`使用的是动态代理的技术，在运行时动态生成目标对象的代理对象，然后在代理对象中对目标对象进行增强，即增添一些功能。

> 关于动态代理，静态代理建议另外详看，非常重要的一个知识点。



接下来我们来看一下`AOP`中的一些非常重要的概念

#### `JoinPoint` 连接点

就是我们想要进行方法增强的那些方法

#### `PointCut` 切入点

切入点是用来匹配相应的连接点的一个概念。

> `    @Pointcut("@annotation(com.cwh.springboot.springboot_aop.annotation.Log)")
>     public void logPointCut(){}`
>
> 这就是定义了一个匹配规则的切入点

#### `Advice` 通知

其实就是在切入点前后执行的方法，包括如下几种通知

* `Before` 前置通知，在切入点目标方法执行之前执行的方法
* `After`后置通知，在切入点目标方法执行之后执行的方法
* `Around`环绕通知 定义在切入点目标方法执行之前执行相关操作和之后执行相关操作的方法
* `AfterReturning` 后置返回通知，在目标方法正常返回后执行，可以在通知中绑定返回值
* `AfterThrowing` 后置异常通知，在方法抛出异常退出时执行，也可以在通知同绑定抛出

> 不同`Advice`拦截顺序
>
> * 该方法只有一个`Aspect`
>
>   无异常：`@Around`（`proceed()`之前的部分） → `@Before` → 方法执行 → `@Around`（`proceed()`之后的部分） → `@After` → `@AfterReturning`
>
>   有异常：`@Around`（`proceed()`之前的部分） → `@Before` →抛异常→  `@After` → `@AfterThrowing`（因为抛出异常了，所以`@Around`中`proceed()`后面的部分也就不执行了）
>
> * 一个方法有多个`Aspect`
>
>   通过为`@Aspect`设置`@Order(num)`来定义优先顺序，`@Order`中的值越小越先执行
>
>   若没有设置，便和线程一样，谁先谁后不一定
>
> Tips:
>
> * 如果在同一个 `aspect` 类中，针对同一个 `pointcut`，定义了两个相同的 `advice`(比如，定义了两个 `@Before`)，那么这两个 `advice` 的执行顺序是无法确定的，哪怕你给这两个 `advice` 添加了 `@Order` 这个注解，也不行。
> * 对于`@Around`，不管它有没有返回值，但是必须要方法内部，调用一下`jointPoint.proceed()`;否则，`Controller` 中的接口将没有机会被执行，从而也导致了 `@Before`不会被触发。

#### `Aspect` 切面

其实就是包括了一些`PointCut`和`Advice`的模块



### 使用示例

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot-AOP)

#### pom.xml

添加`aop`依赖

```xml
        <!--AOP依赖-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```



#### application.yml

```yaml
server:
  port: 8090
```



#### annotation

##### Log

```java
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

```

这里我们自定义一个注解以标记`JointPoint`

> 这里简单介绍一下自定义注解
>
> 我们使用`public @interface 注解名{注解体}`的方式去定义一个注解
>
> 自定义注解需要使用一些元注解去设置注解的一些属性
>
> `@Target`：用于定于该注解可以用在什么地方
>
> ​	`METHOD` 方法声明
>
> ​	`TYPE` 类，接口或`enum`声明
>
> ​	`PARAMETER` 参数声明
>
> ​	`Constructor` 构造器声明
>
> ​	...
>
> `@Doucumented` 用于标记在生成`javadoc`的时候是否将注解包含进去
>
> `@Retention` 用于标明注解的声明周期
>
> ​	`RUNTIME` 运行时级别 存在于源码，字节码，JVM中，用于运行时通过反射获取相关信息
>
> ​	`SOURCE` 源码级别 只存在与源码中，用于和编译器交互
>
> ​	`CLASS` 字节码级别 存在与源码和字节码中, 主要用于编译是产生额外的文件
>
> `@Inherited` 用于标记被标注的类的子类可以继承父类的注解，通过反射获取父类的注解
>
> 注解体中我们设置注解的一些属性，格式为`数据类型 属性名() default 默认值`



#### Aspect

`Springboot`中设置切入点主要有两种方式

1. 自定义注解，在连接点加上该注解，然后在`@Pointcut("@annotation(地址)")`中添加注解在项目中的位置

   > `  @Pointcut("@annotation(com.cwh.springboot.springboot_aop.annotation.Log)")`

2. 使用`execution`语句`@Pointcut(execution(方法修饰符(可选)返回类型 类路径 方法名 参数 异常模式(可选))`

   > ```java
   > 1）execution(public * *(..))——表示匹配所有public方法
   > 2）execution(* set*(..))——表示所有以“set”开头的方法
   > 3）execution(* com.cwh.service.AccountService.*(..))——表示匹配所有AccountService接口的方法
   > 4）execution(* com.cwh.service.*.*(..))——表示匹配service包下所有的方法
   > 5）execution(* com.cwh.service..*.*(..))——表示匹配service包和它的子包下的方法
   > ```

##### LogAspect

```java
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
```

这里使用的是`@Around`通知做日志记录，我们传入`ProceedingJoinPoint joinPoint`为切入点。

`joinPoint.proceed()`为执行被代理对象的方法。



##### WebAspect

```java
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
```

该前面主要是使用了一个前置通知获取`HttpRequest`中的一些参数，以及一个后置返回通知打印返回值。

> 前置通知中我们传入的为`JoinPoint`对象而非`ProceedingJoinPoint`对象，因为除了`Around`通知，其他都不能调用方法的执行，所以`ProceedingJoinPoint`只能在`Around`通知中使用

> `RequestContextHolder.getRequestAttributes()`获取 对`servlet`所有的请求参数，通过`RequestContextHolder`管理保存
>
> `HttpServletRequest request = attributes.getRequest()` 获取`request`信息
>
> `HttpServletResponse response = attributes.getResponse()` 获取`response`信息

> 我们使用`joinPoint.getSignature()`获取被代理对象的签名，包括**修饰符**，**包名**，**类名**，**方法名**
>
> `.getDeclaringTypeName()` 获取类名
>
> `.getName()`获取方法名
>
> `.getTarget()` 获取被代理对象
>
> `.getArgs()`获取参数

后置通知中我们可以通过设置`returning ="xxx"`，这样方法参数中便可以传入返回值（参数名必须也注解中一样）。常用于对返回值的增强处理。



#### Controller

##### TestController

```java

@RestController
public class TestController {

    @Log
    @GetMapping("/log")
    public String test(){
        return "----------------------loading---------------";
    }
}
```

因为程序很简单，所以只需一个非常简单的`controller`即可。



#### 运行结果

##### `GET` `/log`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210610140316.png" style="zoom:150%;" />

我们可以看到控制台在程序执行前实现了功能增强

打印除了程序执行的时间，请求`url`,请求方式，调用的方法，参数，返回值加强。



### 总结

`AOP`很简单，重点是要掌握面向切面的编程思想。

`AOP`之后在权限验证方面非常重要，具体操作后面再继续介绍。