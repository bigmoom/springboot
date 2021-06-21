# SpringBoot整合Redis实现缓存

### Redis

`redis`简单来说就是一款基于内存的以键值对方式存储数据的非关系型数据库。

优点非常明显，由于存储在内存中，所以读取非常快。这种速度是`mysql`等数据库完全不能比拟的，所以我们常用`redis`作为缓存以减少对本地数据库的查询。

> `redis`可以作为系统进程间的资源共享，例如使用`redis`实现`spring session`共享等等

> 关于`redis`的安装与操作这里就不多说了，百度有非常详细的教程，个人还是建议将`redis`部署在`linux`虚拟机或者服务器上。



### 缓存

缓存的作用就是将数据存储在内存中，之后查询同样的数据便可以直接从内存中获取而不是再次执行方法。这样毫无疑问可以省下很多时间。

例如我有一个方法`getById(Integer id)`，通过`id`到数据库中查询并获取实体类。我第一次查询的时候并没有任何缓存，所以执行该方法，通过`mybatis`到数据库中获取数据。但是当第二次查询的时候，如果还是查之前传入的`id`，那么程序便发现缓存中有这个结果，便直接到内存中获取结果，而不是执行方法。



### SpringBoot中的应用

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88Redis)

#### Mysql准备

因为只是简单`demo`，所以这里还是继续使用之前的`Customer`表

```mysql
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) DEFAULT NULL COMMENT '姓名',
  `age` smallint DEFAULT NULL COMMENT '年龄',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8

```

建立连接

```yaml
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/springboot?characterEncoding=utf-8&serverTimezone=GMT%2B8
    username: root
    password: 1234
    
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log.impl: org.apache.ibatis.logging.stdout.StdOutImpl
  type-aliases-package: com.cwh.springboot.redis.model.entity
  mapper-locations: classpath:mapper/*Mapper.xml

```



##### Customer

```java
@Data
@TableName("customer")
public class Customer {
    //    设置主键，主键生成策略
//    AUTO为数据库自增
    @TableId(type = IdType.AUTO)
    private Long id;

    //    @TableField(value="",exist=true)
//    映射非主键字段 value字段名 exist 标明该属性是否在数据库中
    @TableField(value = "name")
    private String name;

    private Integer age;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
//    fill 自动填充策略
//    INSERT 插入式填充，UPDATE 更新时填充,INSERT_UPDATE插入更新时填充
    private Date createTime;

    @TableField(value = "modify_time",fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
}
```



#### Redis的简单使用

首先我们看下`springboot `中对于`redis`的配置

##### pom.xml

```xml
<!--        redis-->
        <dependency>
            <groupId>redis.clients</groupId>
            <artifactId>jedis</artifactId>
            <version>3.6.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
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
    jedis:
#      连接池配置
      pool:
#        连接池最大连接数
        max-active: 8
#        连接池最大阻塞等待时间（负值表示没有限制）
        max-wait: -1
#        连接池最大空闲连接
        max-idle: 8
#        连接池最小空闲连接
        min-idl: 0
```

这里我们连接`db0`，并且配置`jedis`连接池

> 连接池主要作用就是进行连接的缓冲，避免重复创建和销毁连接对象，提高性能



##### Redistemplate

`redis`在`springboot `中默认提供了两种`template`：

`StringRedisTemplate<String,String>`：实现键值对都是`String`类型的操作

`RedisTemplate<Object,Object>` 泛型类，为`StringRedisTempalte`的父类，一般用于自定义设定类型键值对的`redistemplate`

```java
    /**
     *  设置redisTemplate<String,Obejct>序列化
     * @param factory
     * @return
     */
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(factory);

//        使用JackSon2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//        配置ObjecetMapper
        ObjectMapper om = new ObjectMapper();

//        指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
//        setVisibility(forMethod,visibility)
//        用来替换默认序列化检测（默认public fileds或者public getXXX()）
//        formethod 为受影响属性（field/getter/setter）
//        visibility 设置属性最小设定（可以是PUBLIC,ANY,PRIVATE）
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//      指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常 
        			activeDefaultTyping(PolymorphicTypeValidator,ObjectMapper.DefaultTyping,JsonTypeInfo.As)
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
//        配置redistemplate序列化
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
```

上述代码主要实现了对`redis`进行键值对为`<String,Object>`的操作`template`



其实自定义`Redistemplate`主要工作就是设置键值对的序列化问题

```java
public class StringRedisTemplate extends RedisTemplate<String, String> {
    public StringRedisTemplate() {
        this.setKeySerializer(RedisSerializer.string());
        this.setValueSerializer(RedisSerializer.string());
        this.setHashKeySerializer(RedisSerializer.string());
        this.setHashValueSerializer(RedisSerializer.string());
    }
```

`StringRedisTemplate`主要就是设置了序列化为`StringRedisSerializer.UTF_8`

倘若我们什么都不设置，也是能够存到`redis`中，但是使用的是默认的`JdkSerializationRedisSerializer`，这是将数据以二进制流的形式存在`redis`中，操作性差，可读性差。

所以我们这里使用`JackSon2JsonRedisSerialize` 替换默认序列化

> `JackSon2JsonRedisSerialize` 就是将数据以`JSON`形式存储在`Redis`中
>
> <img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621145610.png" style="zoom:80%;" />

我们需要对`JackSon2JsonRedisSerialize`进行配置

`public void setObjectMapper(ObjectMapper objectMapper)` 设置`ObjectMapper`

> `ObjectMapper`是`Jackson`解析`Json`的最简单的方法，可以将`java`对象解析为`JSON`，也可以解析`JSON`创建`java`对象（即反序列化）

这里我们通过`ObjectMapper`设置序列化的域和序列化输入的类型

- `setVisibility(forMethod,visibility)`：指定要序列化的域

  - `forMethod`： 受影响的属性（`filed/getter/setter`）
  - `visibility`：属性最小设定（`PUBLIC/ANY/PRIVATE`）
  - `om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)`即为设定序列化所有访问权限的所有属性

- `activateDefaultTyping(PolymorphicTypeValidator,ObjectMapper.DefaultTyping)`：作用是序列化的时候将对象全类名一起保存下来，方便反序列化

  > 网上很多教程都是使用`objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL)`
  >
  > 这是旧版已被抛弃的做法，说是有重大安全隐患

  - `PolymorphicTypeValidator`：多态验证器，主要用于获取类名
  - `ObjectMapper.DefaultTyping`：序列化默认类型，`NON_FINAL`非`final`对象



`jackson2JsonRedisSerializer`设置完毕之后只需要对`redistemplate`的键值对分别进行序列化设置即可

由于我自定义为`Redistemplate<String,Object>`，所以对`KEY`还是使用`StringRedisSerializer`，对`VALUE`使用我们定义的`jackson2JsonRedisSerializer`

```java
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer)
```



这样我们对于`<String, Object>`键值对的`RedisTemplate`便设置完毕，接下来看看简单的操作和结果

```java
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CustomerService customerService;

    /**
     * 添加<NAME,CUSTOMER>的数据
     * @param name
     * @return
     */
    @PostMapping("/add")
    public Customer addCustomer(@RequestParam("name")String name){
        Customer customer = customerService.getByName(name);
        redisTemplate.opsForValue().set(name,customer);
        return customer;
    }
```

这里我们首先获取传入的`name`参数，然后调用`service`中方法获取`Customer`对象，之后将`name`为`KEY`，`Customer`对象为`VALUE`存入`redis`中

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621155021.png" style="zoom:80%;" />

可以看到存储成功

> 这里使用的是`RDM`工具连接`redis`进行可视化管理

`redisTemplate.opsForValue().set(key,value)`：为`springboot`对`redis`相关操作的封装

​	`opsForValue`即为对字符串的操作，

​	`set(key,value)` 向数据库总添加数据

> 具体原生`redis`操作指南可以见[这个链接](http://doc.redisfans.com/)，写的很全面



#### Spring Cache

`spring cache`其实就是一个使用`AOP`思想，实现了基于注解的缓存功能

只需要在需要进行缓存的方法上加上`@Cacheable()`等注解即可完成注解

##### 配置

首先我们配置本工程使用缓存为`redis`

```yaml
  cache:
    type: redis
```



定义我们的缓存配置类

```
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig extends CachingConfigurerSupport {

    /**
     * 配置cacheManager
     * @return
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory){
//      redisCacheManager构造器需要提供一个redisCacheWriter和一个redisCacheConfigurer
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory);
//      配置cache 序列化为jsonSerializer
        RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer();
        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer);
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair);
//      设置默认过期时间一天
        defaultCacheConfig.entryTtl(Duration.ofDays(1));

//        也可以通过builder来构建
//        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultCacheConfig).transactionAware().build();
        return new RedisCacheManager(redisCacheWriter,defaultCacheConfig);

    }
```

这里通过添加`@EnableCaching`开启缓存，继承`CachingConfigurerSupport`表明这是一个缓存配置类

我们主要的任务便是定义自己的`cacheManager`

`cacheManager` ：顾名思义，即对缓存进行一些管理配置

构建`cacheManager`有两种方式：

1. `new RedisCacheManager(redisCacheWriter,redisCacheConfig)`：构造器构建，传入一个缓存的`writer`对象和一个缓存的配置对象
2. `RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(defaultCacheConfig).transactionAware().build()`：调用`.builder().build()`方法构建，并可以对属性赋值

这里有两个概念

1. `redisCacheWriter`即缓存写对象，可以通过`nonLockingRedisCacheWriter()`和`lockingRedisCacaheWriter`构建，分别对应有锁写和无锁写
2. `defaultCacheConfig` 缓存配置对象，配置缓存一些属性，例如设置缓存的序列化方和缓存过期时间等等

通过对构建和设置这两个对象即可完成对`cacheManager`的配置



##### `@Cacheable`

```java
    @Cacheable(cacheNames = "customer",key = "#id" )
    public Customer getCustomerById(Integer id) {
        log.info("===========调用方法============");
        return baseMapper.selectById(id);
    }
```

我们通过添加`@Cacheable`标明将该放回返回值放入缓存中

他有几个属性：

* `cacheNames/value` ：指定缓存组件的名字，可以简单理解为存在数据库中的表名，但是不准确
* `key` ：缓存数据使用的`key`，可以用它来指定，`key="#param"`可以指定参数值，也可以是其他属性
* `keyGenerator` ：`key`的生成器，用来自定义`key`的生成，与`key`为二选一，不能兼存
* `condition`：设定指定情况下才缓存 `conditon="#id>0"`
* `unless`：否定缓存，当`unless`中条件为`true`时，方法返回值不会被缓存 `unless = "result==null"`
* `sync` ：是否使用异步模式



```java
    @Cacheable(cacheNames = "customer",key = "#id" )
    public Customer getCustomerById(Integer id) {
        log.info("===========调用方法============");
        return baseMapper.selectById(id);
    }
```

我们这里通过输出日志测试是否调用方法还是直接从缓存中拿值

第一次请求：

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621175031.png" style="zoom:80%;" />

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621175658.png)

控制台显示第一次请求调用了方法

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621175217.png" style="zoom:80%;" />

`redis`中也成功添加了缓存

第二次请求：

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621175725.png)

可以发现这次并没有调用方法，而是直接到缓存中拿值



##### `@CachePut`

```java
//  cacheput 即先调用方法，再更新缓存中数据
    @CachePut(cacheNames = "customer",key = "#customer.id")
    public Customer updateCustomer(Customer customer){
        baseMapper.updateById(customer);
        return customer;
    }
```

`@CachePut`标明先调用方法，然后在缓存中更新数据

属性与`@Cacheable`一样



##### `@CacheEvict`

```java
    @CacheEvict(cacheNames = "customer",key = "#id" ,beforeInvocation = false)
    public int deleteById(Integer id){        
        return baseMapper.deleteById(id);
    }
```

`@CacheEvict`标明以清除缓存

属性：

* `key`：指定要删除的数据

* `beforeInvocation` ：缓存的清除是否在方法执行之前执行,
  * `true`表示在方法前删除，`false`表示在方法后删除，如果出现异常则不会清除（`true`情况下必然会删除）
  * 默认为`false`
* `allEntries` ：标明是否要清除所有的数据



##### `@Caching()`

```java
    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "customer",key = "#name" )
            },
            put = {
                    @CachePut(cacheNames = "customer",key = "#result.id")
            }
    )
    public Customer getByName(String name){
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",name);
        return baseMapper.getByName(queryWrapper);
    }
```

定义复杂的缓存规则，里面可以嵌套多个`@Cacheable,@CachePut,@CacheEvict`



##### `keyGenerator`

我们前面提到过可以通过自定义`keyGenerator`即可实现存入数据时`KEY`可以是我们期待的格式

```java
@Component
public class MyKeyGenerator implements KeyGenerator {

//    定义项目前缀
    private String prefix = "redis";

    @Override
    public Object generate(Object target, Method method, Object... objects) {
        char sp =':';
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(sp);
//        类名
        stringBuilder.append(target.getClass().getSimpleName());
        stringBuilder.append(sp);
//        方法名
        stringBuilder.append(method.getName());
        stringBuilder.append(sp);
//        参数名
        if(objects.length>0){
            for(Object object: objects){
                stringBuilder.append(object);
            }
        }
        else {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }
}
```

我们通过实现`KeyGenerator`接口，实现`generate`方法定义`KEY`的生成规则

`target`：目标对象，可以通过`target.getClass().getSimpleName()`获取类名

`method`：目标方法，可以通过`method.getName()`获取方法名

`objects`：参数名



```java
    @Autowired
    private MyKeyGenerator myKeyGenerator;

    @Cacheable(cacheNames = "getall",keyGenerator = "myKeyGenerator" )
    public List<Customer> getAll(){
        return baseMapper.getAll();
    }

```

这里简单定义一个方法，标明使用我们自定义的`myKeyGenerator`生成`KEY`



![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210621180542.png)

可以看到`redis`中存储的`KEY`按照逻辑生成



### 总结

这里只是简单介绍了`redis`作为缓存在`springboot`中的使用，其实`redis`的用途还有很多，例如使用`redis`实现`session`共享等等。

`redis`数据库的相关操作可以参考[这个链接](http://doc.redisfans.com/)

