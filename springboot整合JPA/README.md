# Springboot 整合 JPA

### 简介

`JPA`即`JAVA Persistance API` 是一种官方提出的`ORM`规范。

> 什么是ORM？
>
> `ORM`即`Object-Relational Mapping`。他的作用是在关系型数据库和对象之间作一个映射，这样我们就不需要使用具体的`sql`语句 ，而是像平常一样操作对象即可。例如`Hibernate`就是很著名的`ORM`框架

`spring-data-jpa`便是`spring`基于`Hibernate`开发的一个`JPA`框架。极大的简化了`JPA`的写法，可以非常简单的实现`CRUD`和分页操作。

>  复杂的sql操作也可以使用`@Query("SELECT * FROM TABLE WHERE user_name = :name")`，不过复杂的sql操作还是建议整合mybatis或者mybatis-plus。

### 使用示例

[程序源码](https://github.com/bigmoom/springboot/tree/main/springboot%E6%95%B4%E5%90%88JPA)

#### 建表

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

因为要连接数据库，并且使用`spring data jpa`，所以添加如下依赖

```xml
    <!-- 数据库连接 -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <!--jpa-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
```

#### application.yml

`Springboot`参数配置

```yml
#端口设置
server:
  port: 8090

#数据源设置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    #设置编码和时区
    url: jdbc:mysql://localhost:3306/springboot?characterEncoding=utf-8&serverTimezone=GMT%2B8
    username: root
    password: 1234

   #jpa设置
  jpa:
    #显示sql语句
    show-sql: true
    #format sql 语句
    hibernate:
      naming:
        #驼峰匹配
        physical-strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
        #根据实体类自动更新表结构
      ddl-auto: update
```

`Spring Data Jpa`默认使用驼峰命名法进行匹配

例如实体属性为`userName`，则匹配数据库表中`user_name`列

若不想使用驼峰匹配可以在属性上设置`@Column(name = "user_name")`进行匹配。

> `jpa.hibernate.ddl-auto`使用需要注意，该参数设置为每次启动`hibernate`自动根据实体类对表进行操作。
>
> `update`：这是最常使用的，用来在每次启动`hibernate` 根据实体类更新表结构。
>
> `create`：这就是最为要注意的，因为每次启动`hibernate`都会删除之前建的表，重新建表。
>
> `create-drop`：即加载`hibernate`时创建表，但是关闭之后表自动删除。

#### Entity

```java
@Data
@Entity
@Table(name="customer")
@EntityListeners(AuditingEntityListener.class)
public class Customer {
//  主键
    @Id
//  自增
//  自增策略由数据库控制
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    private Integer age;
//  自动修改创建更新时间
    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date modifiedTime;
}
```

实体类有五个属性`id`,`name`,`age`,`createTime`,`modifyTime`与表中`id`,`name`,`age`,`create_time`,`modify_time`一一对应。

`@Id`标明这是主键

`@GeneratedValue(strategy = GenerationType.IDENTITY)`标明主键是自增的,`strategy`为自增策略。

> `strategy`有五种策略
>
> `TABLE`：spring-data-jpa 会自动建立一个`hibernate_sequence`表存放自增值
>
> `SEQUENCE`：根据数据库的序列生成主键（主要用于Oracle等不支持主键自增长的数据库）
>
> `IDENTITY`：使用数据库主键自增策略
>
> `AUTO`  程序自动选择一种策略

`@Column(name = "user_name")`标明该属性对应表中的`user_name`列，可以不加，如果开启且满足驼峰匹配）。

`@CreatedDate` 和`@LastModifiedDate`是`spring data jpa`提供的非常方便的自动修改数据库中表信息创建和修改时间。但是该实体类需要加上`@EntityListeners(AuditingEntityListener.class)`，且启动类需要加上`@EnableJpaAuditing`，以开启修改权限。

`@Entity`标明这是一个实体类，只有添加了这个注释，`spring data jpa`才会去将它与数据库中的表进行映射。

`@Table` 标明该实体类对应数据库中的表名称，与`@Entity`一样，也是可加可不加。

`@Data`是插件`lombok`中的一个注释，他包含了常用的 `get(),set()`和无参全参构造器等等，帮我们省略了很多代码。

#### 数据库访问对象（DAO）

```java
public interface CustomerRepository extends JpaRepository<Customer,Integer> {

    LinkedList<Customer> findByName(String name);

}
```

我们可以看到`spring data jpa`中建立数据库访问对象的方法是继承`JpaRepository<,>`，其中泛型括号中第一个参数为**实体类**，第二个对象为**主键类型**。

这相当于是建立了一个实体类仓库，仓库中对应这数据库中的该实体类映射的所有数据信息。

其实`spring data jpa`提供了不只这一种接口。

|             接口             | 说明                                                         |
| :--------------------------: | ------------------------------------------------------------ |
|       `JpaRepository`        | 是`PagingAndSortingRepository`的子接口，增加了一些功能，比如`findAll()` |
| `PagingAndSortingRepository` | 是`CrudRepository`的子接口，添加分页和排序的功能             |
|       `CrudRepository`       | 是`Repository`的子接口，提供`CRUD`的功能                     |
|         `Repository`         | 最顶层的接口，是一個空的接口，目的是为了统一所有`Repository`的类型，且能让组件扫描的时候自动识别 |

所以我们进行数据库简单操作的时候不需要自己编写具体代码，只需要继承相关接口，直接调用其中某些方法就行了。

> 我们之所以不用写这些接口的实现类，是因为`Spring Data JPA` 最终将每个这样的`bean`最终映射到了一个统一的实现类`SimpleJpaRepository`的代理对象，而这个代理对象能支持所有每个自定义的`JpaRepository`接口定义的功能和`SimpleJpaRepository`类中定义的所有功能。

##### 查询方法

`spring data jpa`提供两种查询方法

1. 根据方法命名规范查询

   例如`findByName(String name)`，`findAll()`

   | 关键字            | 方法命名                       | 对应 sql 语句              |
   | ----------------- | ------------------------------ | -------------------------- |
   | And               | findByNameAndPwd               | where name= ? and pwd =?   |
   | Or                | findByNameOrSex                | where name= ? or sex=?     |
   | Is,Equals         | findById,findByIdEquals        | where id= ?                |
   | Between           | findByIdBetween                | where id between ? and ?   |
   | LessThan          | findByIdLessThan               | where id < ?               |
   | LessThanEquals    | findByIdLessThanEquals         | where id <= ?              |
   | GreaterThan       | findByIdGreaterThan            | where id > ?               |
   | GreaterThanEquals | findByIdGreaterThanEquals      | where id > = ?             |
   | After             | findByIdAfter                  | where id > ?               |
   | Before            | findByIdBefore                 | where id < ?               |
   | IsNull            | findByNameIsNull               | where name is null         |
   | isNotNull,NotNull | findByNameNotNull              | where name is not null     |
   | Like              | findByNameLike                 | where name like ?          |
   | NotLike           | findByNameNotLike              | where name not like ?      |
   | StartingWith      | findByNameStartingWith         | where name like ‘?%’       |
   | EndingWith        | findByNameEndingWith           | where name like ‘%?’       |
   | Containing        | findByNameContaining           | where name like ‘%?%’      |
   | OrderBy           | findByIdOrderByXDesc           | where id=? order by x desc |
   | Not               | findByNameNot                  | where name <> ?            |
   | In                | findByIdIn(Collection<?> c)    | where id in (?)            |
   | NotIn             | findByIdNotIn(Collection<?> c) | where id not in (?)        |
   | True              | findByAaaTue                   | where aaa = true           |
   | False             | findByAaaFalse                 | where aaa = false          |
   | IgnoreCase        | findByNameIgnoreCase           | where UPPER(name)=UPPER(?) |

2. 使用`@Query()`查询

   ```java
   @Query(value = "select * from customer c where c.name=?1")
   List<Customer> finByName(String name)
   ```

   个人觉得如果需要使用复杂`sql`语句，还是建议使用`mybatis/mybatis-plus`。

   

#### Service

```java
public interface CustomerService {

    public Page<Customer> getAll(Integer pageNum, Integer pageSize);

    public Customer save(Customer customer);

    public LinkedList<Customer> searchByName(String name);
}

```



#### ServiceImpl

```java
@Service
public class CustomerServiceImpl implements CustomerService {

//    注入数据仓库
    @Autowired
    private CustomerRepository customerRepository;
    
//    分页操作
    @Override
    public Page<Customer> getAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum,pageSize);
        Page<Customer> page = customerRepository.findAll(pageable);
        return page;
    }

//    对数据库修改之后的保存操作
    @Override
    public Customer save(Customer customer) {
       return customerRepository.save(customer);
    }

//    查询操作
    @Override
    public LinkedList<Customer> searchByName(String name) {
        return customerRepository.findByName(name);
    }

}
```

服务层是我们对功能逻辑实现的模块。

所以我们这边提供一些接口，以供控制层传入参数使用。

对于接口的实现类，我们首先注入我们自己定义的数据仓库，之后在具体方法内调用对数据仓库的具体操作。

`demo`中定义了三个基本功能

##### 分页

由于我们的数据仓库继承了`PagingAndSortingRepository`接口，所以我们只需传入一个`Pageable`对象即可。

> `pageable` 是 `spring data`里面的一个接口，可以通过`PageRequest.of(,)`传入**页码**，**分页大小**，**排序方式** 进行构建，也可以只传**页码**和**分页大小**，**排序**默认不排序。
>
> ```java
>  	//PageRequest的构造方式
> 	public static PageRequest of(int page, int size) {
>         return of(page, size, Sort.unsorted());
>     }
> 
>     public static PageRequest of(int page, int size, Sort sort) {
>         return new PageRequest(page, size, sort);
>     }
> 	//继承了AbstractPageRequest
> 	//super(page,size)其实只是设定了page和size
>     protected PageRequest(int page, int size, Sort sort) {
>         super(page, size);
>         Assert.notNull(sort, "Sort must not be null!");
>         this.sort = sort;
>     }
> ```

所以我们这里首先传入`pageNum` `pageSize`构建一个`pageable`对象

然后调用数据仓库的`findAll(pageable)`方法，即可返回一个`page`对象

`page`对象中包含了该页的所有数据以及一些相关信息，我们可以通过调用`page.getContent()`获取内容`List`

##### 保存

我们所有的对数据库的增，改都需要调用数据仓库的`save()`方法

`save()` 参数传入实体类对象，数据仓库实现对数据库中该实体类对应信息的修改操作

##### 查询

这里查询便是使用的上述查询的第一种方式 **根据方法命名规范查询**

`customerRepository.findByName(name)` 数据仓库识别方法名，即通过`Name`属性到数据库中进行查询。

>  查询方法返回可以是一个实体类对象，也可以是一个实体类对象列表



#### VO

返回视图类主要包括信息码，返回信息，和具体数据。

我这里简单就只有一个`CustomerVO`，即实体类对象的一些信息。

##### ResultVO

```java
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {
    private Integer code;
    private String msg;
    private T data;
}
```

##### CustomerVO

```java
@Data
public class CustomerVO {

    @JsonProperty(value = "id")
    private Integer id;
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "age")
    private Integer age;
    @JsonProperty(value = "createTime")
    private String createTime;
    @JsonProperty(value = "modifiedTime")
    private String modifiedTime;

    public CustomerVO setByCustomer(Customer customer){
        CustomerVO customerVO = new CustomerVO();
        customerVO.setId(customer.getId());
        customerVO.setName(customer.getName());
        customerVO.setAge(customer.getAge());
        customerVO.setCreateTime(customer.getCreateTime().toString());
        customerVO.setModifiedTime(customer.getModifiedTime().toString());
        return customerVO;
    }
}
```



#### Controller

比较简单的`controller`，就不多赘述，相关信息见注释

```java
@RestController
@RequestMapping("/customer")
public class CustomerController {
//  注入service接口
    @Autowired
    private CustomerService customerService;

    /**
     * 分页展示
     * @param pagenum 页码（第一页为0）
     * @param pagesize 分页大小
     * @return
     */
    @GetMapping("/getall")
    public ResultVO<LinkedList<CustomerVO>> getAll(@RequestParam("pagenum")Integer pagenum,@RequestParam("pagesize")Integer pagesize) {

        Page<Customer> page = customerService.getAll(pagenum,pagesize);
//        获取页内数据内容
        List<Customer> customerList = page.getContent();

//        设置VO
        ResultVO<LinkedList<CustomerVO>> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("succuss");
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList<CustomerVO>();
        for(Customer customer:customerList){
            CustomerVO customerVO = new CustomerVO().setByCustomer(customer);
        }

        resultVO.setData(customerVOLinkedList);
        return resultVO;
    }
    /**
     * 添加用户
     * @param name
     * @param age
     * @return
     */
    @PostMapping("/add")
    public ResultVO<CustomerVO> addCustomer(@RequestParam(value="name",required = true)String name,@RequestParam(value="age",required = true)Integer age){

        Customer customer = new Customer();
        customer.setAge(age);
        customer.setName(name);
//        保存修改
        customerService.save(customer);
//        设置VO
        ResultVO<CustomerVO> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("add success");

        CustomerVO customerVO = new CustomerVO().setByCustomer(customer);

        resultVO.setData(customerVO);
        return resultVO;
    }
    /**
     * 通过名字查询
     * 可以有同名存在，所以返回List
     * @param name
     * @return
     */
    @GetMapping("/search")
    public ResultVO<LinkedList<CustomerVO>> searchByName(@RequestParam(value="name",required = true)String name){

        ResultVO<LinkedList<CustomerVO>> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("search success");
//        获取查询结果
        LinkedList<Customer> customerList = customerService.searchByName(name);
//        设置VO
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList();
        for(Customer customer:customerList){
            CustomerVO customerVO = new CustomerVO().setByCustomer(customer);
            customerVOLinkedList.add(customerVO);
        }
        resultVO.setData(customerVOLinkedList);
        return  resultVO;
    }
}
```

#### 运行结果

##### `GET` `/getAll`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608101733.png" style="zoom:80%;" />

##### `POST` `/add`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608102406.png" style="zoom:80%;" />

##### `GET` `/search`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210608102336.png" style="zoom:80%;" />

### 总结

怎么说呢，`Spring Data Jpa`给我们提供了非常简单的`CRUD`和**分页排序**操作，但是呢，对复杂的查询就没什么优势了。

所以个人建议，只有简单`CRUD`操作的可以用`Spring Data Jpa`，复杂的还是推荐`Mybatis/Mybatis-plus`。
