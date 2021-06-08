# Springboot 整合 Mybatis

### 简介

> `MyBatis` 是一款优秀的持久层框架，它支持自定义 `SQL`、存储过程以及高级映射。
>
> `MyBatis` 免除了几乎所有的 JDBC 代码以及设置参数和获取结果集的工作。
>
> `MyBatis` 可以通过简单的 XML 或注解来配置和映射原始类型、接口和 `Java POJO（Plain Old Java Objects)`为数据库中的记录。

简单来说，`mybatis`作用和之前`spring data jpa`类似，都是实现数据库中数据与实体类的映射。

### 使用示例

[程序源码]()

##### 建表

我们这里首先简单构建`customer`表，该表有`id`,`name`,`age`,`create_time`,`modify_time`属性，其中主键为`id`，递增。

```sql
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer`(
`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
`name` VARCHAR(64) DEFAULT NULL COMMENT '姓名',
`age` SMALLINT(3)  DEFAULT NULL COMMENT '年龄' ,
`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`modify_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY(`id`)
)ENGINE=INNODB DEFAULT CHARSET=utf8;
```



#### pom.xml

```xml
<!--数据库连接-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
<!--mybatis-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.1.4</version>
</dependency>
```



#### application.yml

```yaml
server:
  port: 8090

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/customer?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2B8
    username: root
    password: 1234
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  #设置实体类包名，这样mapper.xml中就可以使用实体类类名
  type-aliases-package: com.cwh.springboot.springboot_mybatis.dao.entity
  #设置mapper存放位置,匹配以Mapper.xml结束的文件
  mapper-locations: classpath:mapper/*Mapper.xml
  #开启驼峰匹配
#  configuration:
#    map-underscore-to-camel-case: true
```

> 这里虽然说是添加了`type-aliases-package`，但是实际使用时`mapper.xml`还是会识别不到实体类，所以添加`resources/mapper/Util.xml`工具配置文件
>
> ```xml
> <?xml version="1.0" encoding="UTF-8"?>
> <!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
> <!--
>     注意: 这个配置文件并不会被实际使用到, 写这个文件仅仅是为了让 IDEA 的 Mybatis 插件找到实体类的位置
>  -->
> <configuration>
>     <typeAliases>
>         <package name="com.cwh.springboot.springboot_mybatis.dao.entity"/> <!--替换成你的项目的实体类的路径-->
>     </typeAliases>
> </configuration>
> ```

#### Entity

因为这次只是简单介绍`mybatis`的`CRUD`操作，所以还是使用单个`Customer`实体类

```java
@Data
public class Customer {
    private Long id;
    private String name;
    private Integer age;
    private Date createTime;
    private Date modifyTime;

}
```

#### VO

##### ResultVO

```JAVA
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {
    private Integer code;
    private String msg;
    private T data;
    
    public ResultVO(){
        this(ResultCode.SUCCESS, null);
    }
    public ResultVO(T data) {
        this(ResultCode.SUCCESS, data);
    }
    public ResultVO(ResultCode resultCode,T data){
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.data = data;
    }
}
```

##### CustomerVO

```java
@Data
public class CustomerVO {
    private Long id;
    private String name;
    private Integer age;
    private Date createDt;
    private Date modifyDt;

    public CustomerVO(Customer customer){
        this.id = customer.getId();
        this.name = customer.getName();
        this.age = customer.getAge();
        this.createDt = customer.getCreateTime();
        this.modifyDt = customer.getModifyTime();
    }
}
```

这次我使用了一个枚举类`ResultCode`以集中设置我的返回信息码和信息。

```java
@Getter
public enum ResultCode {

    SUCCESS(0000,"操作成功"),
    //其余为权限验证，登录验证时候使用，暂时没有用到
    UNAUTHORIZED(1001,"没有登录"),
    FORBIDDEN(1002,"没有权限"),
    VALIDATE_FAILED(1003,"参数校验失败"),
    FAILED(1004,"接口异常"),
    ERROR(1005,"未知错误");

    private int code;
    private String msg;

    ResultCode(int code, String msg){
        this.code=code;
        this.msg=msg;
    }
}
```

#### Mapper

```java
@Mapper
public interface CustomerMapper {

    List<Customer> getAllCustomer();

    Customer getCustomerByName(@Param("name") String name);

    Integer addCustomer(@Param("customer") Customer customer);

    Integer updateCustomer(@Param("customer") Customer customer);

    Integer deleteAllCustomer();

    Integer deleteCustomerById(@Param("id")Integer id);
}
```

`mybatis`中使用`mapper`进行实体类与数据库数据的映射

1. 我们创建`Mapper`接口（使用`@Mapper`标识），定义相关功能方法。

   > 方法中若有多个参数，使用`@Param("")`标识参数名称，若只有一个参数可以不用添加。
   >
   > 可以在启动类上添加`@MapperScan("mapper放置的位置")`省去`@Mapper`注释

2. 创建与`Mapper`接口对应的`mapper.xml`文件

   > `mapper.xml`文件一般建立在`main/resources/mapper`中

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.cwh.springboot.springboot_mybatis.dao.mapper.CustomerMapper">
<!--    构建映射map-->
    <resultMap id ="CustomerMap" type="com.cwh.springboot.springboot_mybatis.dao.entity.Customer">
<!--   id标明为主键 column标明数据库中对应列明 property标明实体类中属性 -->
        <id column="id" property="id" javaType="Long" />
        <result column="name" property="name" javaType="String" />
        <result column="age" property="age" javaType="Integer"/>
        <result column="create_time" property="createTime" javaType="Date" />
        <result column="modify_time" property="modifyTime" javaType="Date"/>
    </resultMap>
<!--    自定义sql语句，避免重复使用-->
    <sql id="Customer_po">
        id , name ,age
    </sql>

    <select id="getAllCustomer" resultMap="CustomerMap">
        select * from customer;
    </select>

    <select id="getCustomerByName" resultMap="CustomerMap" parameterType="java.lang.String">
        select * from customer where name=#{name};
    </select>

    <insert id="addCustomer"  parameterType="Customer">
        insert into customer (name, age ,create_time,modify_time)
        values(#{customer.name},#{customer.age},now(),now());
    </insert>

    <update id="updateCustomer" parameterType="Customer">
        update customer
        set name =#{customer.name},age =#{customer.age},modify_time =now()
        where id = #{customer.id};
    </update>

    <delete id="deleteAllCustomer" >
        delete from customer;
    </delete>

    <delete id="deleteCustomerById"  parameterType="Integer">
        delete from customer where id=#{id};
    </delete>
</mapper>
```

`<mapper namespace=...>`：为该`mapper.xml`对应的`mapper`接口的地址

`<resultMap></resultMap>`：为一个映射对象，我们在这里面实现映射

​	`type`：为对应实体类

​	`<id  >` : 数据库中主键

​	`column`：数据库中对应列名

​	`property`：对应实体类属性

​	`javaType`：对应属性类型

`<sql id="name"></sql>`：定义sql语句，后面可以使用`<include refid="name"/>`引入，避免重复使用

`<select/update/delete/insert>`：即标明对应的`CRUD`操作，内容为具体的`sql`语句

​	`id` ：对应`mapper`接口中方法名字

​	`parameterType`：参数类型

​	`resultType`：返回类型

​	`resultMap`：也是返回映射关系，用于返回一个实体类

> `resultType` 一般用于返回单个值，也可以返回实体类，但需要开启驼峰匹配

​	`#{name}`：传参

> 传入的是实体类对象时，使用`#{customer.age}`传入属性值



#### Service

```java
public interface CustomerService {
    public List<Customer> getAll();
    public Customer searchByName(String name);
    public Integer add(Customer customer);
    public Integer update(Customer customer);
    public Integer deleteAll();
    public Integer deleteById(Integer id);
}
```



#### ServiceImpl

```java
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public List<Customer> getAll() {
        return customerMapper.getAllCustomer();
    }

    @Override
    public Customer searchByName(String name) {
        return customerMapper.getCustomerByName(name);
    }

    @Override
    public Integer add(Customer customer) {
        return customerMapper.addCustomer(customer);
    }

    @Override
    public Integer update(Customer customer) {
        return customerMapper.updateCustomer(customer);
    }

    @Override
    public Integer deleteAll() {
        return customerMapper.deleteAllCustomer();
    }

    @Override
    public Integer deleteById(Integer id) {
        return customerMapper.deleteCustomerById(id);
    }
}
```

`serviceimpl`还是和`spring-data-jpa`类似，注入`Dao`层映射对象，调用`mapper`完成相应操作



#### Controller

```java
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("getall")
    public ResultVO<List<Customer>> getAll(){
        List<Customer> customerArrayList = customerService.getAll();
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList();
        for(Customer customer:customerArrayList){
            CustomerVO customerVO = new CustomerVO(customer);
            customerVOLinkedList.add(customerVO);
        }
        ResultVO<List<Customer>> resultVO = new ResultVO(customerVOLinkedList);
        return resultVO;
    }


    @PostMapping("/add")
    public ResultVO add(@RequestParam("name")String name,@RequestParam("age")Integer age){
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAge(age);
        customerService.add(customer);
        ResultVO resultVO = new ResultVO();
        return resultVO;
    }

    @GetMapping("/search")
    public ResultVO searchByName(@RequestParam("name")String name){
        Customer customer = customerService.searchByName(name);
        ResultVO resultVO = new ResultVO(new CustomerVO(customer));
        return resultVO;
    }

    @PutMapping("/update")
    public ResultVO  updateById(@RequestParam("id")Long id,@RequestParam("name")String name,@RequestParam("age") Integer age){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setAge(age);
        customer.setName(name);
        customerService.update(customer);
        return new ResultVO();
    }


    @DeleteMapping("/delete")
    public ResultVO deleteAll(){
        customerService.deleteAll();
        return new ResultVO();
    }

    @DeleteMapping(value = "/delete",params = {"id"})
    public ResultVO deleteById(@RequestParam(value = "id",required = true)Integer id){
        customerService.deleteById(id);
        return new ResultVO();
    }
}
```

`Controller`完成简单的`CRUD`操作，都很简单，就不详细说明

> 我们可以发现`deleteAll()`和`deleteById`都是匹配的`/customer/delete`这个`url`
>
> 但是我们想根据不同的参数对象匹配不同的方法
>
> 我们只需要在`DeleteMapping`中设置`params ={""}`即可，设定之后参数就是必须需要的。



#### 运行结果

##### `GET`  `/getall`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162522.png" style="zoom:80%;" />

##### `POST` `/add`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162359.png" style="zoom:80%;" />

##### `GET` `/search`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162448.png" style="zoom:80%;" />

##### `PUT` `/update`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162623.png" style="zoom:80%;" />

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162720.png" style="zoom:80%;" />

##### `DELETE` `/delete`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162905.png" style="zoom:80%;" />

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162930.png" style="zoom:80%;" />

##### `DELETE`  `/delete?id=`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162823.png" style="zoom:80%;" />

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608162839.png" style="zoom:80%;" />

### 总结

`mybatis`与`spring data jpa`相比，更加贴近原生`sql`,所以对于数据库的复杂操作更加方便，但是简单的`CRUD`就显得有点过于复杂。

所以我们接下来就要学习`mybatis`的增强`mybatis-plus`。