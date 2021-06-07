# Springboot 整合 JPA

### 简介

`JPA`即`JAVA Persistance API` 是一种官方提出的`ORM`规范。

> 什么是ORM？
>
> `ORM`即`Object-Relational Mapping`。他的作用是在关系型数据库和对象之间作一个映射，这样我们就不需要使用具体的`sql`语句 ，而是像平常一样操作对象即可。例如`Hibernate`就是很著名的`ORM`框架

`spring-data-jpa`便是`spring`基于`Hibernate`开发的一个`JPA`框架。极大的简化了`JPA`的写法，可以非常简单的实现`CRUD`和分页操作。

>  复杂的sql操作也可以使用`@Query("SELECT * FROM TABLE WHERE user_name = :name")`，不过复杂的sql操作还是建议整合mybatis或者mybatis-plus。

### 使用示例

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

