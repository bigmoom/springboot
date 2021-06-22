# Springboot整合springsession实现session共享

### 简介

`session`我们之前有介绍过（可见[springboot整合springsecurity](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88springsecurity)），简单来说就是将用户信息或者数据存储在服务器上，通常用于验证用户身份或者避免通过获取相关信息。

但是，缺点也是非常明显：

1. 占用服务器负载：我们可以使用`token`，用时间换取空间

2. 对于多服务器环境，`session`无法共享

对于第二点缺点，我们目前有几种比较常用的解决方法

1. 使用`cookie`加密的方式将`session`保存在客户端上

   优点是可以减轻服务器压力，缺点是每次请求都要带上`cookie`信息，占用一定带宽。此外若用户禁用`cookie`则无法使用

2. 服务器间同步

   通过配置`tomcat`集群，在集群中广播自己的`session`信息，但是缺点很明显，当集群规模较大时，会占用大量资源来进行`session`同步处理

3. 基于分布式缓存的`session`共享机制

   将`session`缓存到`redis`中，这样不同服务器都可以直接到内存中获取`session`，效率高，也最常用



### Springsession

> `springSession`是 `spring` 旗下的一个项目，把 `servlet` 容器实现的 `httpSession`替换为`springSession`，专注于解决`session`管理问题。可简单快速且无缝的集成到我们的应用中。

`springsession`就是`spring`的一个框架，实现了我们上面说的基于分布式缓存的`session`共享机制



#### 操作实例

我们先来看看简单的操作实例

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88springsession%E5%AE%9E%E7%8E%B0session%E5%85%B1%E4%BA%AB)

##### pom.xml

```xml
<!--        springsession-->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
            <version>2.5.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>2.5.1</version>
        </dependency>
```



##### application.yml

```yaml
spring:
  redis:
    #    数据库索引，默认为0
    database: 0
    #    redis host ip
    host: 192.168.56.129
    #    redis  连接端口
    port: 6379
    #    服务器连接密码（默认为空）
    password:
    #    连接超时时间（毫秒）
    timeout: 1000
    
#设置springsession存储类型，默认为redis
  session:
    store-type: redis
```



##### 启动类加上`@EnableRedisHttpSession`

```java
@SpringBootApplication
@EnableRedisHttpSession
public class SpringsessionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringsessionApplication.class, args);
    }
}
```



##### SessionController

这里我们构建一个简单的`controller`测试是否实现了`session`共享

```java
@RestController
@RequestMapping("/session")
public class SessionController {

    /**
     * 设置session
     * @param request
     * @param attributes
     * @return
     */
    @PostMapping("/set")
    public Map<String,Object> setSession(HttpServletRequest request, @RequestParam("attributes")String attributes){
        request.getSession().setAttribute("attributes",attributes);
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("SessionID:",request.getSession().getId());
        return map;
    }

    /**
     * 获取session
     * @param request
     * @return
     */
    @GetMapping("/get")
    public String getSession(HttpServletRequest request){
        String attributes = (String) request.getSession().getAttribute("attributes");
        return attributes;
    }
}
```

两个方法都很简单

`setSession`：通过请求中的参数设置`session`中的`attributes`

`getSession`：测试是否能获取在其他端口设定的`session`中对应`attributes`的值



##### 运行结果

由于要体现`session`共享，所以我们这里将在两个不同端口运行程序

> 通过`IDEA`不同端口启用同一个项目，可以在右上角运行处选择`edit config`，然后添加`springboot`项目，并同意`parallel run`

`port:8080` `/session/set`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622111930.png" style="zoom:80%;" />

可以看到成功设置了`session`并且返回了`SessionID`

`port:8090` `/session/get`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622112152.png" style="zoom:80%;" />

成功获取到了`session`中的值，标明我们实现了`session`共享

我们再来看看`redis`存储的`session`

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622112403.png)

这里以`sessionID`为`KEY`,`session`为`value`存储，但是`sessionID`前面的命名空间太长了，而且不具有项目标识，我们可以通过在`application.yml`中设置`namespace="xx"`即可

#### Springsession 逻辑分析

我们可以看到`springsession`使用非常简单，对于用户几乎不用进行什么操作，那么`springsession`具体为我们做了什么工作呢？

其实`springsession`通过`autoconfigure`帮我们自动配置了一个过滤器`SessionRepositoryFitlter`

##### `SessionRepositoryFitlter`

```java
@Order(-2147483598)
public class SessionRepositoryFilter<S extends Session> extends OncePerRequestFilter {
```

定义来看：

* 这里为`SessionRepositoryFilter`设定了一个非常小的`order`值，以确保能够在`filterchain`中被优先执行。

  > 优先执行是为了将原生`HttpRequest`进行替换和封装

* 继承了`OncePerRequestFilter`，确保一次请求只通过一次

> 源码命名太长，直接贴代码太乱了，这里就直接截图了

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622120056.png)

可以看到，我们这里首先对`request`和`response`进行封装，之后将封装请求传入`doFilter()`

所以具体逻辑就在两个封装类中，这里我们着重关注`request`的封装类

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622133706.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622133749.png)

获取`session`的逻辑如下：

1. `this.getCurrentSession()` 检查`serlvet`容器中是否有`session`，如果有`session`则直接返回，如果没有则去`redis`中去拿

2. `this.getRequestedSession()` 根据请求中的信息获取`sessionID`，然后根据`sessionID`去`redis`中获取`session`

   <img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622134400.png" style="zoom:80%;" />

   这里并不是每次查询都是去`redis`中查询，而是设置了一个`session`缓存，每次查询先检查缓存中有没有，如果有则直接拿值，如果没有则通过`httpSessionIdResolver.resolveSessionIds(this)`获取`sessionID`

   > 获取`sessionid`有两种方式，一种是根据请求中`Header`信息获取，一种是放在`cookie`中
   >
   > 我们这里`httpSessionIdResolver = new CookieHttpSessionIdResolver()`选择使用`cookie`获取`sessionID`

   查询到`session`之后，更新`session`相关信息并返回

3. 如果在`redis`中没有找到，则根据`create`判断是否创建新的`session`



基本的逻辑非常清楚也非常好理解，我们再来具体看看`springsession`是如何在`redis`中查询`session`的

##### `SessionRepository`

`springsession`为我们提供了这样一个`session`仓库，能够完成对`session`的`CRUD`操作

我们这里使用的是`RedisSessionRepository`，实现了将`session`存储在`redis`上的`CRUD`操作

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622140240.png)

这就是我们`getRequestedSession()`中使用的`findById()`

可以看到里面就是对`redis`数据库的直接操作



### 总结

本文主要介绍了`springboot`如何整合`springsession`实现`session`共享，也简单介绍了其中逻辑原理，具体的部分还是建议仔细阅读源码。

