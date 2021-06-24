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

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88rabbitmq)

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
#    correlated: 发布消息成功或失败到达交换机时触发回调方法
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

##### publisher

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



定义好了这些配置之后我们就可以开始发送数据了

我们使用`springboot`提供的`RabbitTemplate`进行消息的发送

> `RabbitTemplate`提供了对于`rabbitmq`非常多的操作，包括设置交换机，设置路由键，设置队列等等

```java
    @PostMapping("/direct")
    public String sendDirectMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("DirectExchange","DirectRouting",messageMap);

        return messageMap.toString();
    }
```

我们这里使用`convertAndSend`对消息进行转变和发送

> `convertAndSend(exchange,routing key,msg)`
>
> `exchange`：消息发至的交换机
>
> `routing key` ：消息携带的路由键，用于交换机发送至相应队列
>
> `msg`：消息
>
> 该方法首先对`msg`进行类型验证，看是否为`message`类，如若不是进行转换，之后进行`send()`

发送`message=huihui`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624094234.png" style="zoom:80%;" />

在`ip:15672` `rabbitmq`管理界面可以看到，队列已经收到一条消息

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624094338.png)

##### consumer

消息发送成功，接下来就要配置消息的消费者

我们直接配置`consumer`

```java
@Component
//设置监听队列
@RabbitListener(queues = "DirectQueue")
public class DirectConsumer {

    @RabbitHandler
    public void process(Map message){
        System.out.println("DirectConsumer:收到消息:"+message);
    }
}
```

这里我们使用了两个注解

`RabbitListener(queues="")`：设置该消费者监听哪个队列，即队列中有了消息我就拿来消费

`RabbitHandler`：设置自动确认，即从队列中拿到消息后就算确认消费了，不管消息是不是我要的消息，也不管之后的逻辑处理。

> 手动确认就是可以在逻辑中选择确认消费以及不确认，从而将消息返回队列或者直接扔掉

配置完成后我们就可以直接消费队列中的消息了



此外，我们在消费端可以定义一下队列

```java
@Configuration
public class QueueConfig {

    @Bean
    public Queue directQueue(){
        return new Queue("DirectQueue");
    }
```

这是因为在启动程序时，如果消息发布端还没有发布过消息，那么`rabbitmq`服务器内是没有对应队列的，所以消费服务会直接报错终止。



我们现在来启动消费端

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624095056.png)

可以看到，控制台成功输出消息，代表消费端成功消费了消息

我们再来看看`rabbitmq`服务

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624095204.png)

刚刚的待消费消息已被消费



倘若有多个消费端，对应一个队列呢？

我们这里再定义一个消费者

```java
@Component
@RabbitListener(queues = "DirectQueue")
public class DirectConsumer2 {
    @RabbitHandler
    public void process(Map message){
        System.out.println("DirectConsumer2:收到消息:"+message);
    }
}
```

我们再发送消息

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624095435.png)

从控制台输出可以明显看出，多个消费端采用轮询的策略

> 轮询策略只适用于短时间内，可以设定一个类似过期时间的属性，超过这个时间没有消费过消息就重新开始轮询

> 可以通过设置 `channel.basicQos(1)`阻止`rabbitmq`将消息平均分配，会优先发给不忙的消费者，如果当前消费者忙的话会发送给下一个消费者



#### FanoutExchange

##### publisher

```java
@Configuration
public class FanoutRabbitConfig {

    @Bean
    public Queue fanQueueA(){
        return new Queue("FanQueueA");
    }

    @Bean
    public Queue fanQueueB(){
        return new Queue("FanQueueB");
    }

    @Bean
    public Queue fanQueueC(){
        return new Queue("FanQueueC");
    }


    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange("FanoutExchange");
    }

    @Bean
    public Binding bindingExchangeA(){
        return BindingBuilder.bind(fanQueueA()).to(fanoutExchange());
    }

    @Bean
    public Binding bindingExchangeB(){
        return BindingBuilder.bind(fanQueueB()).to(fanoutExchange());
    }

    @Bean
    public Binding bindingExchangeC(){
        return BindingBuilder.bind(fanQueueC()).to(fanoutExchange());
    }
}
```

配置与上面的`DirectRabbitConfig`类似，不过`fanoutexchange`不需要设置路由键，只需要将队列和交换机绑定起来即可



发送数据

```java
    @PostMapping("/fanout")
    public String sendFanoutMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("FanoutExchange",null,messageMap);

        return messageMap.toString();
    }
```

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624100938.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624101116.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624101213.png)

可以看到，虽然我只发了一次消息，但是所有绑定过这个交换机的队列都收到了这个消息



##### consumer

```java
@Component
@RabbitListener(queues = "FanQueueA")
public class FanoutConsumerA {

    @RabbitHandler
    public void process(Map testMessage) {
        System.out.println("FanoutReceiverA消费者收到消息  : " +testMessage.toString());
    }
}
```

与`DirectConsumer`类似，只是监听队列不同

启动消费端

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624101437.png)

可以看到绑定了`FanoutExchange`的队列都获得了消息，消费者也都消费成功



#### TopicExchange

##### publisher

```java
@Configuration
public class TopicRabbitConfig {

//    设置两个topic
    public static final String man = "Consumer.man";
    public static final String woman = "Consumer.woman";

//    全匹配队列
    @Bean
    public Queue allTopicQueue(){
        return new Queue("AllTopicQueue");
    }

//    单一匹配队列
    @Bean
    public Queue manTopicQueue(){
        return new Queue("ManTopicQueue");
    }


    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange("TopicExchange");
    }

//    绑定单一匹配队列和交换机
//    设置匹配路由为唯一键，即与direct相同
    @Bean
    public Binding bindManTopic(){
        return BindingBuilder.bind(manTopicQueue()).to(topicExchange()).with(man);
    }

//    绑定全匹配队列和交换机
//    设置匹配路由为`Consumer.`的主题
    @Bean
    public Binding bindAllTopic(){
        return BindingBuilder.bind(allTopicQueue()).to(topicExchange()).with("Consumer.#");
    }
}
```

这里我们设置两个主题：`consumer.man`,`consumer.woman`

设置一个全匹配队列，匹配所有的`consumer`，即设置路由键`Consumer.#`

设置一个单一匹配队列，只匹配`man`，即设置路由键`Consumer.man`,此时和`directexchange`功能一致



发送数据

```java
    @PostMapping("/topic/man")
    public String sendManMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("TopicExchange","Consumer.man",messageMap);

        return messageMap.toString();
    }

    @PostMapping("/topic/woman")
    public String sendWomanMessage(@RequestParam("message")String message){
        String messageId = String.valueOf(UUID.randomUUID());
        String messageBody = message;
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String,Object> messageMap = new HashMap<String,Object>();
        messageMap.put("id",messageId);
        messageMap.put("body",messageBody);
        messageMap.put("createTime",createTime);

//        设置消息发送的交换机和路由key
        rabbitTemplate.convertAndSend("TopicExchange","Consumer.woman",messageMap);

        return messageMap.toString();
    }

```

我们设置两个请求，一个发送主题为`Consumer.man`的消息，一个发送主题为`Consumer.woman`的消息

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624102242.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624102318.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624102538.png)

可以看到，虽然发送两次，但是队列中总共有三条消息，因为发送的两条消息都被全匹配队列匹配

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624102706.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624102725.png)

可以看到，全匹配队列中获取了发送的两条消息，而单一匹配队列只匹配了`Consumer.man`



##### consumer

全匹配

```java
@Component
@RabbitListener(queues = "AllTopicQueue")
public class AllTopicConsumer {
    @RabbitHandler
    public void process(Map message){
        System.out.println("AllTopicConsumer:收到消息:"+message);
    }
}
```

单一匹配

```java
@Component
@RabbitListener(queues = "ManTopicQueue")
public class ManTopicConsumer {

    @RabbitHandler
    public void process(Map message){
        System.out.println("ManTopicConsumer:收到消息:"+message);

    }
}
```

这里我们设置两个不同的消费者消费对应队列

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624103052.png)

可以看到`AllTopicConsumer`获取了`yiyi`和`huihui`两条消息

而`ManTopicConsumer`只消费了`huihui`这条消息



#### 消息确认

消息的发布和消费我们已经简单了解了，但是作为消息队列，消息可靠性是一个非常重要的性质。

例如消息发布之后可能会因为种种原因丢失或者发送失败，即没有到达交换机又或者交换机没能成功发送到队列中，例如消费者从队列中获取消息后发现这不是我想要的数据或者逻辑处理失败，但是消息已经消费确认了，不能交给其他消费者，这就导致有的消息没有被正确处理。

所以，`rabbitmq`提供了非常重要的功能，**消息确认**

消息确认又分为：

1. 发布者是否成功发布并推送消息

2. 消费者是否消费消息成功



##### 发布消息确认

首先我们需要在配置文件中开启该功能

```java
    publisher-confirm-type: correlated
    publisher-returns: true
```

可以看到，我们这里有两个属性

1. `publisher-confirm-type` ：对应`ConfirmCallBack`回调函数，用以确认消息是否成功到达交换机，默认属性为`None`

   `None`：即不启用该回调函数

   `correlated`：消息成功或失败到达交换机时调用回调函数

   `simple`：具有两个功能

    	1. 与`correlated`一致
    	2. 可以使用`rabbittemplate`调用`waitforconfirm`或`waitForConfirmsOrDie`等待`broker`节点返回发送结果，根据结果来判定下一步逻辑

2. `publisher-return:` 对应`ReturnCallback`，用以确认消息是否成功发送到队列（失败时才会调用）



```java
@Configuration
public class RabbitConfig {

    @Bean
    public RabbitTemplate createRabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);

//        设置mandatory触发回调函数
        rabbitTemplate.setMandatory(true);

//        设置confirmCallBack,即重写confirm()
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("ConfirmCallback:     "+"相关数据："+correlationData);
                System.out.println("ConfirmCallback:     "+"确认情况："+ack);
                System.out.println("ConfirmCallback:     "+"原因："+cause);
            }
        });

//        设置returnCallBack,即重写returnMessage()
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                System.out.println(
                    "ReturnCallback:     "+"消息："+returnedMessage.getMessage());
                System.out.println(
                    "ReturnCallback:     "+"回应码："+returnedMessage.getReplyCode());
                System.out.println(
                    "ReturnCallback:     "+"回应信息："+returnedMessage.getReplyText());
                System.out.println(
                    "ReturnCallback:     "+"交换机："+returnedMessage.getExchange());
                System.out.println(
                    "ReturnCallback:     "+"路由键："+returnedMessage.getRoutingKey());
            }
        });

        return rabbitTemplate;
    }
}
```

我们通过配置自定义`rabbitTemplate`实现发布确认

* `confirmCallBack`： 我们只需要通过重写`confirmCallBack`中的`confirm()`即可

  `confirm(correlationData,ack,cause)`

  ​	`correlateionData`：可以封装业务`ID`信息，需要在发送消息时传入此参数，否则是`null`

  ​	`ack`：消息发送到交换机的结果状态，成功为`true`，失败为`false`

  ​	`cause`：失败原因，如果成功为`null`

* `returnCallBack`：我们只需要重写`returnMessage()`即可

  `returnedMessage(returnedMessage)`

  ​	`returnedMessage`：封装类，包括了发送的消息，返回码，返回信息，交换机和路由键属性

>  `rabbitTemplate.setMandatory(true)`
>
> `mandatory` 属性决定了消息发送失败之后会不会返回给发布者，`false`表示直接丢弃
>
> 所以要想使用`returnCallBack`和`confirmCallBack`必须设置为`true`



接下来我们测试几种情况

* 消息发送到交换机失败

  ```java
      @PostMapping("/test")
      public String testMessageAck(@RequestParam("message")String message){
          String messageId = String.valueOf(UUID.randomUUID());
          String messageBody = message;
          String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
  
          Map<String,Object> messageMap = new HashMap<String,Object>();
          messageMap.put("id",messageId);
          messageMap.put("body",messageBody);
          messageMap.put("createTime",createTime);
  
  //        设置不存在的exchange
          rabbitTemplate.convertAndSend("Exchange","DirectRouting",messageMap);
          return messageMap.toString();
      }
  }
  ```

  这里的`"Exchange"`为未注册的交换机名称，所以消息会找不到该交换机即消息发送到交换机失败

  ![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624112920.png)

  可以看到只调用了`ConfirmCallBack`,并且显示了失败原因`no exchange 'Exchange' in vhost '/'`

  

* 消息成功发送到交换机，但是发送到队列失败

  ```java
   rabbitTemplate.convertAndSend("TestExchange","DirectRouting",messageMap);
  ```

  `"TestExchange"`为已注册交换机，但是没有与任何队列进行绑定

  ![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624113506.png)

  通过控制台可以发现

  `ConfirmCallBack`状态为`true`，即消息成功到达交换机

  `ReturnCallback`又被调用说明消息发送到队列失败，并且显示了失败原因为`NO_ROUTE`，即没有找到队列

  

* 消息成功发送到交换机且成功路由到相应队列

  ```java
  rabbitTemplate.convertAndSend("TestExchange2","TestRouting",messageMap);
  ```

  这里`"TestExchange2"`为成功注册的交换机，且与队列实现了绑定

  ![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624113833.png)

  可以看到控制台只有`ConfirmCallBack`消息，即没有调用`ReturnCallBack`

所以无论消息发送成功与否，都会调用`ConfirmCallBack`，而只有当消息从交换机发送到队列失败的时候才会调用`ReturnCallBack`

> 因为设置了`mandatory`为`true`，所以失败的消息会返回到发布者手里，此时我们可以通过回调函数进行逻辑处理，例如重新发送（重新设置交换机等等），进行错误日志记录



##### 消费消息确认

消息确认主要分为两种模式：

1. 自动确认

   这是`rabbitmq`的默认模式，也就是我们之前直接添加`RabbitHanlder`所选用的模式。这种模式下，只要消费端成功拿到消息即算确认成功，无论消费端后续处理消息是否成功。所以有个很明显的问题，就是如果消息处理失败则意味着该消息被直接抛弃了，不会有其他消费端进行处理。

2. 手动确认

   这就是我们常用的模式，在我们消费端收到消息之后，根据处理情况手动调用确认方式：

   * `basicAck` ：肯定确认
   * `basicNack`：否定确认，可以批量确认
   * `basicReject`：否定确认，只能一次拒绝一条消息

   `void basicNack(deliveryTag,multiple,requeue)`：

   `deliveryTag`：消息唯一标识，自动生成，通过`message.getMessageProperties().getDeliveryTag`获得

   `multiple`：是否开启批量退回，`false`则不开启，开启退回需要增加自己的业务判断逻辑（例如攒够几条再批量退回等等）

   `requeue`是否退回到消息队列，`true`表示退回，交给其他消费者处理，不退回则直接丢弃



```java
@Configuration
public class MessageListenerConfig {

    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    private  MyMessageListener myMessageListener;

    /**
     * 配置listenercontainer,添加自定义listener
     * @return
     */
    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(cachingConnectionFactory);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(10);
//        RabbitMq默认是自动确认，这里改为手动确认
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//      设置queue
        container.setQueueNames("TestQueue");
        container.setMessageListener(myMessageListener);

        return container;
    }
}
```

自定义`listener`添加手动确认逻辑

```java
@Component
public class MyMessageListener implements ChannelAwareMessageListener {

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
//       类似于消息id
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try{
//            对消息的自定义处理
            System.out.println("Message:"+message.toString());
            System.out.println("消息来自："+message.getMessageProperties().getConsumerQueue());
//          确认消息
//          第二个参数会是否开启批处理，true则表示一次性确认小于等于传入值的所有消息
            channel.basicAck(deliveryTag,true);

        }catch (Exception e){
            channel.basicNack(deliveryTag,true,false);
            e.printStackTrace();
        }
    }
}
```

可以看到我们这边定义了一个配置类和自定义`listener`组件



配置类里面我们主要是定义了一个监听容器

`SimpleMessageListenerContainer`：通过该监听容器可以设置监听队列，消费者数量以及设置对应的监听器

`container.setAcknowledgeMode(AcknowledgeMode.MANUAL)` 设置确认方式为手动确认

`container.setQueueNames("TestQueue")` 设置监听队列,可以设置多组队列，通过监听器中区分队列实现不同的逻辑

`container.setMessageListener(myMessageListener)` 设置监听器，这里为我们自定义监听器



自定义监听器中添加了手动确认逻辑

我们一般采用`try catch`的方式编写确认逻辑：

* 在`try`块中编写处理信息的逻辑，处理完之后确认消费 `channel.basicAck(deliveryTag,true)`

* 对于处理失败的我们捕获异常，并且确认不消费，并选择是否退回给队列`channel.basicNack(deliveryTag,true,false)`



我们发送数据测试

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624143121.png)

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210624143133.png)

控制台输出为我们自定义监听器中设置的输出格式

> 对于异常，我们往往是返回到队列交给其他消费者消费，但是如果一直返回到队列一直处理失败就会造成拥塞，所以我们有的时候对于处理失败的消息不返回到队列，而是将消息用日志记录下来或者将消息发送到死信交换机根据死信路由分配到死信消费者

> 死信队列主要是用于确保在一些非常重要的业务中，一些没有被正常消费的消息不被丢弃，我们可以通过配置死信队列，让未被正常处理的消息暂存在另一个队列中，待后续排查清除问题再处理死信消息。

> 死信队列可以参考[这个链接](https://mfrank2016.github.io/breeze-blog/2020/05/04/rabbitmq/rabbitmq-how-to-use-dead-letter-queue/)，个人觉得写的非常详细。



### 总结

本文主要介绍了消息队列，`RabbitMq`以及`Springboot `中如何整合`RabbitMq`。

内容只能作为简单了解，对于`rabbitmq`的使用还有很多知识点，例如集群模式，镜像模式等等。

此外，`rabbitmq`的吞吐量在当今大数据时代已经有点更不上，取而代之的是现在非常火热的`Kafka`和`Rocketmq`，对于这两个消息队列以后有空的话肯定还是会重点学习一下的。
