# Springboot整合RabbitMQ

### MQ

#### 简介

消息队列（`Message Queue`，简称`MQ`），只保存消息的一个容器，本质上是一个容器。

简单来说，消息队列就是一个存储消息的容器，它接受消息发布者发布的消息，然后将消息存储在容器中等待之后的消费者到容器里消费消息。

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622153920.png)

这就是最简单的一个消息队列的模型

`Producer`：消息生产者，负责产生和发布消息到`Broker`中

`Broker`：消息处理中心，负责消息的存储，确认等等，一般其中会包含多个`queue`

`Consumer`：消息消费者，负责从`Broker`中获取消息，并进行相关处理（消费）



#### 功能

`MQ`的存在提供了三个非常重要的功能

##### 解耦

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622154948.png" style="zoom: 50%;" />

例如上图，我们有`A,B,C,D,E`五个系统，原先我`A`系统只要发送数据到`B,C,D`三个系统，但是后来需求增加，现在`E`也需要`A`系统的数据，所以只能在`A`系统中添加相关功能。这只是简单的描述，实际开发中系统要比这复杂的很多，所以就会导致系统耦合度极高。

这时候我们引入`MQ`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622155353.png" style="zoom:50%;" />

系统`A`只需要讲数据发送到`MQ`中，完全不用管哪些系统会消费数据，是否消费成功等等，而需要这个数据的系统直接从`MQ`中拿就好了，也并不需要知道发布者是谁，更不用与发布者产生关系。

可以看到，通过添加一层消息队列，整个系统的耦合度大大降低。



##### 异步

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622155744.png" style="zoom:50%;" />

我们继续看这个场景，用户对`A`发送请求，花费`3+300+450+200= 953ms`，这还是只是简单的几个接口调用，具体开发场景下会有数十个或者上百个接口，如果同步执行，那耗时必然会非常的多。

那么我们使用`MQ`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622163150.png" style="zoom:50%;" />

我们使用消息队列之后，`A`系统只要将数据发送到`MQ`中即可返回，这样对于用户来说，只需要5`ms`即可实现响应，虽然后续操作并没有在5`ms`内完成。而且总的时间也大大降低了，类似于多线程处理（多线程只能在本地上实现，而消息队列可以在不同项目中，不同服务器中运用）



##### 削峰

削峰简单来说就是能够缓解高峰期的峰值并发请求

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622164150.png" style="zoom:50%;" />

假设我们有这样一个系统，平时请求量很少，我们的服务器和数据库完全能应付，但是某个时间段，例如整点秒杀活动，突然涌入5k的请求，而我们的数据库最多只能处理2k的并发请求，这样不进行削峰我们数据库就会直接瘫痪挂掉。

而使用`MQ`之后

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622164603.png" style="zoom:50%;" />

虽然每秒有5k个请求，但是不会直接涌入我的服务器或者数据库，而是进入消息队列中等待我系统的拉取，这样这个系统都是在我的可控范围之内，不会出现服务器或者数据库瘫痪现象。



#### 缺陷

##### 可用性

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622155353.png" style="zoom:50%;" />

就拿这个场景来看，所有的要获取这个数据的系统都连接`MQ`等待消费数据，但是当`MQ`出现故障宕机了，所有的相关系统也就都不能获取数据。这是非常严重的，所以我们要保证消息队列的高可用性（例如集群模式，镜像模式，设置监测节点和备用节点等等）

##### 复杂度增加

通过添加`MQ`确实能解决很多问题，但是可能会导致有些逻辑更加复杂。例如**如何保证消息没有被重复消费**（保证幂等性），保证消息传递顺序等等

##### 事务性

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210622163150.png" style="zoom:50%;" />

上面场景我们虽然能实现用户在5ms内就能收到反馈，但是具体逻辑还在运行，倘若`C`系统执行失败怎么办？

所以我们可以将整个逻辑看作事务进行处理，要么全部成功要么全部失败。



### RabbitMQ

`RabbitMq`其实是`AMQP`协议的一个开源体现，内部就是`AMQP`的实现

> `AMQP`与我们熟悉的消息队列有一点点差别，其中增加了`binding`和`exchange`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210623172022.png" style="zoom:80%;" />

这就是一个经典的`AMQP`模型

`producer` 生产者，发布者

`Exchange` 交换机，根据`Binding`中设置的路由规则将消息发送到相应的`Queue`中

`Binding` 绑定，设置路由匹配规则

`Queues` 队列

`Consumer` 消费者



#### Exchange

上面的大部分元素我们已经非常熟悉了，这里就重点看一下交换机`Exchange`

交换机根据路由规则不同，又分为三种不同的交换机：



##### DirectExchange

顾名思义，**直连交换机**，简单来说就是**单播**

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210623172906.png"  />

消息中的路由键`Routing key`与和`binding`中设置的`binding key`完全一致，交换机就会将消息发送到对应的队列中

##### FanoutExchange

**扇形交换机**，简单来说就是广播

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210623173425.png)

每个发送到扇形交换机的消息都会被发送到与其绑定的所有队列中，不需要路由键

##### TopicExchange

**消息订阅交换机**

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210623173611.png)

消息订阅模式就是当消息携带的路由键值符合设定的路由规则时，将符合的消息发送到绑定的队列中

例如图上红色队列设定的路由键为`usa.#`，表示匹配所有以`usa.`开头的路由键，所以可以收到`usa.news`和`usa.weather`的消息

> `#`匹配1个或多个字符，可以使用`binding key=#`匹配任意字符
>
> `*`匹配1个



所以`RabbitMq`使用非常简单，我们只需要设置交换机，队列，以及使用一定的路由规则将他们绑定在一起即可。



### Springboot 使用 RabbitMQ

由于要使用消息队列需要**消息发布者**和**消费者**，所以本项目分为两个子项目，`publisher`和`consumer`

#### 配置

两个项目的配置都是一样的

##### pom.xml

```xml
<!--        RabbitMQ-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
```



##### application.yml

```yaml
server:
# consumer端口设置为8080
  port: 8090

spring:
#  配置rabbitmq
  rabbitmq:
    host: 192.168.56.129
#  rabbitmq主服务端口为5672,管理页面端口为15672    
    port: 5672
    username: rabbitadmin
    password: rabbitpwd
#   设置回调函数
#   confirm-type: 默认为None即不启用，
#    correlated: 发布消息成功到交换机时触发回调方法
#    simple: 具有两个功能
#    1.与correlated相同
#    2.可以使用rabbittemplate调用waitforconfirm或waitForConfirmsOrDie
#     等待broker节点返回发送结果，根据结果来判定下一步逻辑
    publisher-confirm-type: correlated
    publisher-returns: true

```

>  回调确认函数我们在后面会详细说明



我们将通过实现三种不同的交换机来展示`rabbitmq`在`springboot`中的大概使用方法

#### DirectExchange

```java
@Configuration
public class DirectRabbitConfig {

    /**
     * 设置队列
     * Queue(name,durable,exclusive,autoDelete)
     * name: 队列名称
     * durable: 是否持久化，即是否会被存储到磁盘上，当消息代理重启时仍存在，默认为true
     * exclusive：只能被当前创建的连接使用，而且当连接关闭队列后立即删除，默认为false
     * autoDelete: 是否自动删除，当没有生产者或者消费者使用此队列时自动删除,默认为false
     * @return
     */
    @Bean
    public Queue directQueue(){

        return new Queue("DirectQueue",true);
    }
    /**
     * 设置交换机
     * DirectExchange(name,durable,autoDelete)
     * @return
     */
    @Bean
    public DirectExchange directExchange(){

        return new DirectExchange("DirectExchange");
    }

    /**
     * 设置binding，即路由规则
     * bind(Queue()).to(Exchange()).with(routingkey)
     * @return
     */
    @Bean
    public Binding bindDirect(){
        return BindingBuilder.bind(directQueue()).to(directExchange()).with("DirectRouting");
    }
}
```

这里我们定义一个配置类，主要内容就是：

1. 设置队列

   `return new Queue(name,durable,exclusive,autoDelete)`

   `name`：队列名称，后续用于消费者获取消息

   `durable`：是否持久化，即是否会被存储到磁盘上，当消息代理重启时仍存在，默认为`true`

   `exclusive`：只能被当前创建的连接使用，而且当连接关闭队列后立即删除，默认为`false`

   `autoDelete`：是否自动删除，当没有生产者或者消费者使用此队列时自动删除,默认为`false`

2. 设置交换机

   `return new DirectExchange(name,durable,autoDelete)`

   `name`：交换机名称，后续用于消息发布者发布消息

   `durable`：是否持久化，即是否会被存储到磁盘上，当消息代理重启时仍存在，默认为`true`

   `autoDelete`：是否自动删除，当没有生产者或者队列使用该交换机时自动删除,默认为`false`

3. 绑定交换机和队列

   `return BindingBuilder.bind(Queue()).to(Exchange()).with(routingkey)`

   `Queue()`：为需要绑定的队列

   `Exchange()`：为需要绑定的交换机

   `routingKey`：设置路由键，用于交换机将消息发送到队列中

#### 

