# Springboot整合Mybatis-plus

### 简介

`Mybatis-plus`是基于`mybatis`的纯粹的增强版，并没有修改`mybatis`任何功能，只是单纯的添加了一些非常方便的功能。

- 类似`spring-data-jpa`，通过继承`BaseMapper`无须配置`xml`文件即可实现`CRUD`等操作
- 通过分页插件实现物理分页查询
- 通过继承`IService`和`ServiceImpl`实现例如批量更新等等操作
- 通过条件构造器简化查询 
- ......  

总的来说就是简化了单表查询的操作，但是复杂的多表查询还是需要配置`mapper.xml`手动编写`sql`语句去实现。

`mybatis-plus`是国内的开源插件，[官网](https://mybatis.plus/guide/)对于功能写的非常详细，我这里只是简单介绍一下几个常用的简单功能。

### 使用示例

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88mybatis-plus)

#### pom.xml

```xml
 		<!--连接mysql        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>

        <!--mybatis-plus        -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.4.0</version>
        </dependency>
```



#### application.yml

```yaml
server:
  port: 8090

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/springboot?characterEncoding=utf-8&useSSL=false&autoReconnect=true&rewriteBatchedStatements=true&serverTimezone=UTC
    username: root
    password: 1234

# mybatis-plus
mybatis-plus:
  configuration:
    #驼峰
    map-underscore-to-camel-case: true
    #显示sql语句
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  type-aliases-package: com.cwh.springboot.springboot_mybatisplus.dao.entity
  mapper-locations: classpath:mapper/*Mapper.xml
```

和`mybatis`配置没有区别，基本就是设置数据库连接，设置实体类位置，设置`mapper.xml`匹配位置



#### Entity

这里还是只使用了`Customer`单个实体类

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

`@TableName("customer")` 设置映射对应表名称

`@TableId(type = IdType.AUTO)` 设置主键，`type`为主键策略

> |     值      |                             描述                             |
> | :---------: | :----------------------------------------------------------: |
> |    AUTO     |                         数据库ID自增                         |
> |    NONE     | 无状态,该类型为未设置主键类型(注解里等于跟随全局,全局里约等于 INPUT) |
> |    INPUT    |                    insert前自行set主键值                     |
> |  ASSIGN_ID  | 分配ID(主键类型为Number(Long和Integer)或String)(since 3.3.0),使用接口`IdentifierGenerator`的方法`nextId`(默认实现类为`DefaultIdentifierGenerator`雪花算法) |
> | ASSIGN_UUID | 分配UUID,主键类型为String(since 3.3.0),使用接口`IdentifierGenerator`的方法`nextUUID`(默认default方法) |

`@TableField(value="name")`设置非主键字段映射字段名，可以设置多个属性

> 这里介绍几个常用属性
>
> | 属性  | 默认值            | 是否必须 | 描述                                  |
> | ----- | ----------------- | -------- | ------------------------------------- |
> | value | “”                | 否       | 数据库字段名                          |
> | exist | false             | 否       | 是否为数据库字段名，false则不进行匹配 |
> | fill  | FieldFill.DEFAULT | 否       | 字段自动填充策略                      |
>
> 其中`FieldFill`通常用于自动添加**创建时间**，**更新时间**
>
> |      值       |         描述         |
> | :-----------: | :------------------: |
> |    DEFAULT    |      默认不处理      |
> |    INSERT     |    插入时填充字段    |
> |    UPDATE     |    更新时填充字段    |
> | INSERT_UPDATE | 插入和更新时填充字段 |



#### Config

由于我们要使用到`mybatis-plus`的分页插件，所以我们要设置一个配置类以添加插件

<h5 id=MyBatisConfig>MyBatisConfig</h5>

```java
@Configuration
@MapperScan("com.cwh.springboot.springboot_mybatisplus.dao.mapper")
public class MybatisPlusConfig {

//    设置分页插件（分页拦截器）
//    在每个需要分页的功能模块实现分页
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

`@Configuration` 该注解标注在类上，以标明这是一个配置类，用来配置spring容器，配置类方法上标注`@Bean`将返回对象注册为`bean`对象。

我们这里是将`PaginationInnerInterceptor`添加到`MybatisInterceptor`中，并设置数据库类型为`mysql`

这是一种拦截器策略，是一种典型的`AOP`实现，切入点为传入`Page`对象的方法。

即每次我们传入一个`Page`对象，就会调用分页插件进行分页。



#### VO

与之前都一样，统一格式，没有什么什么区别

##### ResultVO

```java
@Getter
public class ResultVO<T> {

    /**
     * 状态码, 默认1000是成功
     */
    private int code;
    /**
     * 响应信息, 来说明响应情况
     */
    private String msg;
    /**
     * 响应的具体数据
     */
    private T data;

    public ResultVO(T data) {
        this(ResultCode.SUCCESS, data);
    }

    public ResultVO(ResultCode resultCode, T data) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }

    @Override
    public String toString() {
        return String.format("{\"code\":%d,\"msg\":\"%s\",\"data\":\"%s\"}", code, msg, data.toString());
    }
}
```

##### RresultCode

```java
@Getter
public enum ResultCode {

    SUCCESS(0000, "操作成功"),

    UNAUTHORIZED(1001, "没有登录"),

    FORBIDDEN(1002, "没有相关权限"),

    VALIDATE_FAILED(1003, "参数校验失败"),

    FAILED(1004, "接口异常"),

    ERROR(5000, "未知错误");

    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
```

##### CustomerVO

> 没怎么用到

```java
@Data
public class CustomerVO {
    private Long id;
    private String name;
    private Integer age;

    @JsonProperty(value = "creat_time")
    private Date createTime;
    @JsonProperty(value = "modify_time")
    private Date modifyTime;

    public CustomerVO(){};

    public CustomerVO(Customer customer){
        this.age = customer.getAge();
        this.name = customer.getName();
        this.id = customer.getId();
        this.createTime = customer.getCreateTime();
        this.modifyTime = customer.getModifyTime();
    }
}
```

这次使用了一个`ControllerAdvice`进行返回数据的统一处理，统一返回为`ResultVO`格式

##### ResponseControllerAdvice

```java
@RestControllerAdvice(
    basePackages = {"com.cwh.springboot.springboot_mybatisplus.controller"})
public class ResponseControllerAdvice  implements ResponseBodyAdvice<Object> {
    /**
     * 判断是否调用beforeBodyWrite,false为不调用
     * @param returnType
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass) {
//        若返回类型已经是ResultVO,则不调用
        return !returnType.getParameterType().equals(ResultVO.class);
    }
    
    @Override
    public Object beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // String类型不能直接包装，所以要进行些特别的处理
        if (returnType.getGenericParameterType().equals(String.class)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // 将数据包装在ResultVO里后，再转换为json字符串响应给前端
                return objectMapper.writeValueAsString(new ResultVO<>(data));
            } catch (JsonProcessingException e) {
                throw new ApiException("返回String类型错误");
            }
        }
        // 将原本的数据包装在ResultVO里
        return new ResultVO<>(data);
    }
}

```

`@RestControllerAdvice`作为特化`@Component`，允许通过类路径扫描自动检测实现类。通常用于：

- 全局异常处理（`@ExceptionHandler`）
- 全局数据绑定（`@InitBinder`）
- 全局数据预处理（`@ModelAttribute`）

这里我们用于扫描所有的`controller`，实现`ResponseBodyAdvice<Object>`实现返回数据的统一处理。

该接口有两个方法：

- `support(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> aClass)`

  第一个参数为返回数据类型，第二个参数为最终将会使用的消息转换器。

  返回结果决定是否调用`beforeBodyWrite`，`false`不调用，`true`调用

- `beforeBodyWrite(Object data, MethodParameter returnType, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) `

  `data` 返回数据

  `returnType` 返回数据类型

  `mediaType` 响应的`ContentType`

  `aclass` 最终将会使用的消息转换器

  返回为修改后的数据类型。



#### Mapper

##### CustomerMapper

```java
@Repository
public interface CustomerMapper extends BaseMapper<Customer>{

//    分页查询
    IPage<CustomerVO> selectPage(Page<CustomerVO> page, @Param(Constants.WRAPPER)Wrapper<CustomerVO> Wrapper);
//	  添加用户
    Integer addCustomer(@Param("customer")Customer customer);
//	  使用条件构造器查询
    List<Customer> selectByAge(@Param("ew")Wrapper<Customer> Wrapper);
}
```

##### CustomerMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.cwh.springboot.springboot_mybatisplus.dao.mapper.CustomerMapper">
    
<!--    分页查询-->
    <select id="selectPage" resultType="com.cwh.springboot.springboot_mybatisplus.vo.CustomerVO">
        select
            *
        from
            `customer`
        ${ew.customSqlSegment}
    </select>

<!--    添加用户-->
    <insert id="addCustomer" parameterType="Customer">
        insert
        into
            `customer`(name,age)
            values (#{customer.name}, #{customer.age})
    </insert>

<!--    使用条件构造器查询-->
    <select id="selectByAge" resultType="Customer">
        select
            *
        from
            `customer`
        ${ew.customSqlSegment}
    </select>
</mapper>
```

`CustomerMapper`实现了`mybatis-plus`的几个非常常用的功能

##### CRUD接口

###### Mapper CRUD接口

`mybatis-plus`为我们提供了封装好的`BaseMapper`，通过继承`BaseMapper`即能够实现简单的`CRUD`操作

> ```java
> public interface BaseMapper<T> extends Mapper<T> {
>     int insert(T entity);
>     int deleteById(Serializable id);
>     int deleteByMap(@Param("cm") Map<String, Object> columnMap);
>     int delete(@Param("ew") Wrapper<T> wrapper);
>     int deleteBatchIds(@Param("coll") Collection<? extends Serializable> idList);
>     int updateById(@Param("et") T entity);
>     int update(@Param("et") T entity, @Param("ew") Wrapper<T> updateWrapper);
>     T selectById(Serializable id);
>     List<T> selectBatchIds(@Param("coll") Collection<? extends Serializable> idList);
>     List<T> selectByMap(@Param("cm") Map<String, Object> columnMap);
>     T selectOne(@Param("ew") Wrapper<T> queryWrapper);
>     Integer selectCount(@Param("ew") Wrapper<T> queryWrapper);
>     List<T> selectList(@Param("ew") Wrapper<T> queryWrapper);
>     List<Map<String, Object>> selectMaps(@Param("ew") Wrapper<T> queryWrapper);
>     List<Object> selectObjs(@Param("ew") Wrapper<T> queryWrapper);
>     <E extends IPage<T>> E selectPage(E page, @Param("ew") Wrapper<T> queryWrapper);
>     <E extends IPage<Map<String, Object>>> E selectMapsPage(E page, @Param("ew") Wrapper<T> queryWrapper);
> }
> ```
>
> 我们可以直接调用上述方法而不用编写`mapper.xml`

###### Service CRUD 接口

`mybatis-plus`还为我们提供了封装好的`IService`接口，进一步提供`get`,`list`,`remove`,`page`等操作，想要仔细了解可以查看官方文档或者之间查看源码。

##### 分页插件

`mybatis-plus`为我们提供了非常简单的分页插件。

`[MybatisPlusConfig]`的[配置](#MyBatisConfig)已经在上文给出，之后我们只需要编写我们的分页查询功能即可。

`selectPage(page,wrapper)`：第一个参数为一个`page`对象,第二个为一个查询构造器。

> 这里的page对象和之前`springframwork.data.domain`中的`page`对象类似，但是不同。
>
> 我们一样可以通过`page(current,pagesize)`构建一个`page`对象，但是此处`current`从1开始，表示当前所在页码。
>
> `page.addOrder(List<OrderItem> items)`添加排序序列

##### 条件构造器

`mybatis-plus`提供了条件构造器`AbstractWrapper,QueryWrapper,UpdateWrapper`用于生成`sql`中`where`条件。

这里简单介绍几个常用功能

* `QueryWrapper`继承`AbstractWrapper`主要用于查询

  `eq` `querrywrapper.eq("name","cwh")`即等于`where name = cwh`

  `ge` `querrywrapper.ge("age","24")`即等于`where age >= 24`

  `between` `querrywrapper.between("age","24","26")` 即等于 `where age between 24 and 26 `

  `like` `querrywrapper.like("name","王")`即等于 `where name like %王%`

  `orderByAsc` `querrywrapper.orderByAsc("age","id")`即等于 `order by age ASC id ASC`

* `UpdateWrapper`继承`AbstractWrapper`提供了`set`方法，用于赋值更新

* `LambdaQueryWrapper`和`LambdaUpdateWrapper`功能与上述两个没有区别，只是参数使用`lambda`格式

  ```java
  //	王姓，年龄小于40，邮箱不为空
  	LambdaQueryWrapper<User> wq = Wrappers.<User>lambdaQuery();
  	wq.like(User::getName, "张").and(user -> user.lt(User::getAge, 40).or().isNotNull(User::getEmail));
  ```

> 使用注意事项
>
> 需要`mybatis-plus`版本 >= `3.0.7`
>
>  param 参数名要么叫`ew`,要么加上注解`@Param(Constants.WRAPPER)` ，`mapper.xml` 中 使用`${ew.customSqlSegment}`匹配 



#### Service

##### CustomerService

```java
public interface CustomerService extends IService<Customer> {
//  分页
    IPage<CustomerVO> selectPage(Page<CustomerVO> page);
//  添加用户
    void addCustomer(Customer customer);
//  通过用户id查询
    Customer getCustomerById(Integer id);
//  通过用户id删除
    void deleteById(Integer id);
//  查询大于age1,小于age2的用户  
    List<Customer> selectByAge(Integer age1,Integer age2);
}
```

##### CustomerServiceImpl

```java
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {


    @Override
    public IPage<CustomerVO> selectPage(Page<CustomerVO> page) {
//        设置条件构造器
        QueryWrapper<CustomerVO> queryWrapper = new QueryWrapper<>();
        return baseMapper.selectPage(page,queryWrapper);
    }

    @Override
    public void addCustomer(Customer customer) {
        baseMapper.addCustomer(customer);
    }

    @Override
    public Customer getCustomerById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void deleteById(Integer id) {
        baseMapper.deleteById(id);
    }

    @Override
    public List<Customer> selectByAge(Integer age1,Integer age2) {
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<Customer>();
//		where 大于age1,小于age2
        queryWrapper.between("age",age1,age2);
        queryWrapper.orderByAsc("age");
        return baseMapper.selectByAge(queryWrapper);
    }
}
```

`CustomerServiceImpl`继承了`mybatis-plus`提供的通用实现类`ServiceImpl<CustomerMapper,Custome>`并且实现了自己定义的`CustomerService`

所以不用再注入`dao`层，因为`SeviceImpl`中已经为我们实现了注入

> ```java
> public class ServiceImpl<M extends BaseMapper<T>, T> implements IService<T> {
>     @Autowired
>     protected M baseMapper;
>     ....
> ```

所以我们可以通过`baseMapper`直接调用`baseMapper`和我们定义的`CustomerMapper`中的方法



#### Controller

##### CustomerController

```java
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

//    分页查询
    @GetMapping("/page/{current}")
    public IPage<CustomerVO> getPage(@PathVariable("current")int current){
//        分页参数（为mybatis-plus中page，与springdata domain中不同）
//        传入当前
        Page<CustomerVO> page = new Page<>(current,5);

//        设置排序序列
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("create_time");
//        倒叙排列
        orderItem.setAsc(false);
        page.addOrder(orderItem);

        return customerService.selectPage(page);
    }

//    添加用户
    @PostMapping("/add")
    public void add(@RequestParam("name")String name, @RequestParam("age")Integer age){
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAge(age);
        customerService.addCustomer(customer);
    }

//    查询
    @GetMapping("/get")
    public CustomerVO getCustomerById(@RequestParam("id")Integer id){
        Customer customer = customerService.getCustomerById(id);
        return new CustomerVO(customer);
    }
    
//    根据年龄大小查询
    @GetMapping(value = "get", params = {"age1","age2"})
    public List<Customer> selectCustomerByAge(@RequestParam("age1")Integer age1, @RequestParam("age2")Integer age2){
        List<Customer> customerList = customerService.selectByAge(age1, age2);
        return customerList;
    }

//    删除
    @DeleteMapping("delete")
    public void deleteById(@RequestParam("id")Integer id){
        customerService.deleteById(id);
    }
}

```



#### 运行结果

##### `GET` `/customer/page/{current}`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210609180015.png" style="zoom:80%;" />

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210609180043.png" style="zoom:80%;" />

##### `POST` `/add`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210609180233.png" style="zoom:80%;" />

##### `GET` `/get`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210609180420.png" style="zoom:80%;" />

##### `GET` `/get?age1=?&age2=?`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210609180324.png" style="zoom:80%;" />

##### `DELETE` `/delete`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210609180456.png" style="zoom:80%;" />

### 总结

个人感觉`mybaits-plus`更像是`spring data jpa`与`mybatis`的结合升级版。

他提供了简单的`CRUD`和分页的接口，也可以根据复杂需求使用原生`sql`语句。可以实现非常强大的功能。

