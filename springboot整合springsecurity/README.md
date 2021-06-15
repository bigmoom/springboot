# Springboot整合springsecurity

### 简介

我们知道，**登录认证**和**权限验证**是现在每个涉及到用户登录的系统都不必不可缺的。

本文会简单介绍登录认证和权限认证相关的知识，以及如何使用`springsecurity`框架实现相关功能。



### 基础知识

#### 登录认证

登录认证的概念非常简单，主要是处理两个问题，**登录密码校验**和**验证登录状态**

##### 登录密码校验

系统一般都有登录界面，用户在登录界面输入用户名和登录密码。

那么我们要做的就是根据用户名，去用户数据库中查询到用户的密码，然后将数据库中的密码与用户输入的密码进行比对。如果相同，那么登录成功，如果不同，则提示账号密码错误。

##### 认证登录状态

我们要知道`HTTP`请求是一个**无状态**协议，即每次的请求都是相互独立的。服务器不会知晓你之前的行为，只会看到你当前的请求。

所以我们需要一种机制能够将用户的登录状态保存下来，即**凭证**。用户登录之后，每次发送请求的同时携带该凭证，那么服务器就可以通过这个凭证认证你的登录状态。

现在最常用的便是使用`Session`，`Token`，`Cookie`携带凭证。

###### Session

`session`即会话，用户访问服务器是便是与服务器产生了一次`session`。服务器为了记住该用户，则在服务器上使用`session`存储这个用户的信息。

`session`不会随着浏览器的关闭而消失，他具有可定义失效日期，失效后服务器便认为与客户端断开联系，并删除`session`以节约空间。

`session`的缺点很明显，就是会占用服务器的存储空间。当服务器的访问较少时，`session`还是可行的，但是当有大量的访问时，便会占用服务器的负载和性能。

此外，对于分布式的系统，为了负载均衡，同一个访问会分配到不同的服务器，所以一个用户登录之后，将信息保存在登录时的服务器上，但是后续可能就被分配到另外的服务器上，这就导致要重新登录。这是非常不能接受的。

> 当然我们可以使用`springsession`实现分布式会话管理，实现`session`共享功能。

###### Cookie

为了解决`session`保存在服务器上的问题，`cookie`同样由服务器生成（与`session`一样，生成的是`session ID`），但是交由浏览器保存在客户端本地，以`key-value`形式存储用户信息。

其实就是写在客户端的一段`txt`文件，里面包括了你的登录信息，这样下次登录时就会自动调用`cookie`登录。

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210611133943.png)

这就是我使用`Chrome`浏览器中`github`上的`cookies`

> `name`（名字）+`value`（值）+`Domain`（域名）+`Path`（路径）+`Expires`（过期时间）+`Size`（大小）

`cookie`相较`session`对于服务器更加友好，但是由于存储在客户端，容易被修改，所以非常不安全

此外，`cookie`只能保存少量数据，而且是基于使用的浏览器而不同的，不同浏览器访问相同网页使用的`cookie`都是不同的。

###### Token

`token`是目前较为主流的一种凭证，他的基本思想便是提供一个加密和解密的功能

用户登录之后，根据一段数据，可以是用户名或者其他数据，加密成一段字符串，并返回给客户端。客户端脚本会将`token`储存起来，可以是存储到`cookie`也开始是`localStorage`

之后用户再次访问该域名时，便会将`token`放到 `Authorization header`中，此时服务器可以是对该`token`进行解密或者对数据进行再次加密，检查是否匹配。若解密成功或者匹配成功即实现登陆认证。

> 其实`cookie`和`session`只是将服务器生成的`session id`存储到了不同的地方，一个存储在服务器，一个存储在客户端。
>
> 而`token`和他们两个不同的是，`token`是无状态的，他不需要依赖`session`，即服务器不需要保存对`token`的记录，服务器只需要验证他的有效性即可。
>
> 此外，`session`和`cookie`很关键的问题就是跨域问题，因为产生的`session id`是依赖`session`的，所以不同`session`将导致登录认证失败。而`token`由于不依赖`session`所以完全可以实现`CORS(跨域资源共享)`

#### 权限验证

登录认证是对用户身份的认证，而权限认证则是在用户认证之后，对于该用户是否具有访问该接口或者执行该功能的权限的验证。

例如对于一个用户管理的系统，管理员具有对用户的`CRUD`的操作，而普通用户只具有查看的操作。所以当客户端发送请求的时候，我们就要验证该用户是否具有访问当前权限资源的权限。

往往我们将权限实现为权限资源，即对应的访问`url`（`/user/add`）和接口的请求`url`（`POST:/user/add`）

因此，我们将用户划分为几种不同的身份，例如超级管理员，管理员，普通用户等，每种身份对应不同的权限`id`，而每个权限`id`有对应其能访问的权限资源。

我们往往使用`Interceptor`进行权限验证，即每次向某个权限资源发出请求时，启动拦截器，获取用户的权限列表，验证该权限资源是否在用户权限列表当中，若不在则拒绝访问。

> 拦截器
>
> 拦截器其实就是`AOP`的一种运用，就是在某个切入点执行一个通知。例如权限验证，调用方法前打印出字符串等等。
>
> 过滤器
>
> 过滤器是对传入`servlet`之前的`request`或者之后的`response`进行业务逻辑处理，例如过滤掉危险字符等等。	



### SpringSecurity

`springboot`这类安全框架对于`web`系统的支持其实就是提供**基于一个个过滤组成的过滤器链**

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210611145109.png" style="zoom: 67%;" />

我们通过过滤器链中的过滤器完成相应的类似**登录认证**和**权限验证**的功能。

`springsecurity`的具体操作便是在过滤器链中添加一个`FilterChainProxy`过滤器，这个代理过滤器会为服务器创建一套`springsecurity`自定义的过滤器链。

这样便会启用`springsecurity`默认的一些过滤器，类似`UsernamePasswordAuthenticationFilter`负责登录认证，`FilterSecurityInterceptor`负责权限授权。

#### 登录认证

##### 概念介绍

首先我们先介绍几个概念

------

`Authentication` ：`springsecurity`存储认证信息的组件，其中存储内容就是用户认证信息：

:bell:`Principal` 用户信息，没有认证时一般是用户名，认证后一般是用户对象。

:bell:`Credentials`用户凭证

:bell:`Authorities`用户权限

`SecurityContext`上下文对象，用来获取`Authentication`

`SecurityContextHolder`上下文管理对象，用来在程序任何地方获取`SecurityContext`

------

了解了这几个概念之后就很好理解`springsecurity`的认证流程了：

* 初次登录验证（验证密码是否相同）
* 登录之后设置`authentication`，将其放入上下文`securityContext`中，代表该用户已登录。

* 之后发出其他请求时，从`securityContextHolder`中获取`securityContext`，然后校验是否存在`authentication`，如果存在则代表通过认证，不存在则不通过。

我们可以看到，主要的任务便是如何设置`authentication`

`springsecurity`提供了一个组件`AuthenticationManager`去实现这个功能，我们通过`authenticationManager.authenticate(...)`设置`authentication`

但是，要想设置`authentication`我们还需要用户信息，以及如何对密码进行校验

这里`spring security`又提供了三个组件

***

`UserDetails` : 通过该接口告知系统我们的用户对象数据，提供了用户名，密码，权限等通用属性，`spring security`为我们提供了一个实现类`User`，以避免重写多余方法，我们只需要继承`User`类，调用其构造方法初始化用户名，密码，权限即可。

`UserDetailsService` ：该接口提供`loadUserByusername`方法，用于通过用户名查询用户对象，我们只需要实现实现该接口，在`loadUserByusername()`完成我们的逻辑并返回`UserDetail`对象即可。

`PasswordEncoder`：用于对密码进行加密。

***

##### UserDetail

```java
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class UserDetail extends User {
//	实体类
    private UserEntity userEntity;

    public UserDetail(UserEntity userEntity, Collection<? extends GrantedAuthority> authorities) {
//      调用父类的构造器方法，传入用户名，密码和权限列表
        super(userEntity.getUserName(),userEntity.getUserPassword(),authorities);
        this.userEntity = userEntity;
    }
}

```

我们这里自定义一个`UserDetail`类继承`spring security`为我们提供的`User`实现类

通过调用父类构造器，我们便可以传入`username`,`password`，和权限列表

> `@EqualsAndHashCode(callSuper = false)` 用来调用父类属性与子类属性一同生成`hashcode`



##### UserServiceImpl

```java
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper,UserEntity> implements UserService,UserDetailsService{

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private JWTManager jwtManager;

    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 实现loadByUserName(),返回需要的userDetail
     * @param name
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
//      调用mapper获取用户对象
        UserEntity userEntity = baseMapper.selectByUserName(name);
//      如果没查到，抛出异常
        if(userEntity == null){
            throw new UsernameNotFoundException("用户没有找到");
        }
//      获取用户权限列表

        Set<SimpleGrantedAuthority> authorities = resourceService.getResourceByUserId(userEntity.getId())
                .stream()
                .map(String::valueOf)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
//      返回我们需要的额userDetail对象
        return new UserDetail(userEntity,authorities);
    }

```

这里是我们的一个`service`的实现类，实现了`spring security`提供的`UserDetailService`，所以我们只需要实现`loadUserByUsername`即可获取我们需要的`userdetail`

> 这只是部分代码，仅供认识，也可以结合源码理解

> `Set<SimpleGrantedAuthority> authorities `这是我们之后用于权限验证的用户权限列表



##### PasswordEncoder

```java
public interface PasswordEncoder {
    /**
 	 * 加密
 	 */
    String encode(CharSequence rawPassword);
    /**
 	 * 将未加密的字符串（前端传递过来的密码）和已加密的字符串（数据库中存储的密码）进行校验
 	 */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

`PasswordEncoder`接口实现两个方法，加密和匹配

加密用于我们注册的时候将用户输入的密码加密后存储在数据库中

而匹配主要是用于登录认证时，我们将用户输入的密码与数据库中密码进行匹配

```java
@Bean
public PasswordEncoder passwordEncoder() {
    // 这里我们使用bcrypt加密算法，安全性比较高
    return new BCryptPasswordEncoder();
}
```

这里我们通过采用`BCrypt`加密实现自己的`passwordEncoder`



##### 配置AuthenticationMangaer

```java
............   
/**
     * 配置Authenticationmanager
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth)throws Exception {
//        添加自定义的userDetailService和passEncoder
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder());

    }

```

我们在配置类中对`authenticationManagerBuilder`添加我们自定义的`userService`和`passwordEncoder`以构建我们需要的`authenticationManager`



##### 认证异常处理器

`spring security`也为我们提供了一些认证异常`AuthenticationException`已经对于这类认证异常的处理器`AuthenticationEntryPoint`，用于非`/login`请求的登录认证

> ```java
> //      如果没查到，抛出异常
>         if(userEntity == null){
>             throw new UsernameNotFoundException("用户没有找到");
>         }
> ```
>
> 这里我们抛出的`UsernameNotFoundException`即为`AuthenticationException`的一个具体实现类

当用户访问资源时，如果抛出`AuthenticationException`，则会触发`ExceptionTranslationFilter`调用`AuthenticationEntryPoint`中的`commence()`来处理认证失败逻辑。

我们只需要自定义一个类去实现`AuthenticationEntryPoint`接口即可

```java
@Slf4j
public class MyEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException {
        log.error(e.getMessage());
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = response.getWriter();
        ResultVO<String> resultVO = new ResultVO<>(ResultCode.UNAUTHORIZED, "没有登录");
        out.write(resultVO.toString());
        out.flush();
        out.close();
    }
}
```

这里我们实现`commence()`返回**没有登录**的返回视图



##### JWT

我们前面说过了`session`，`token`，`cookie`的区别

这里我们采用`JWT(JSON WEB TOKEN)`工具即采用`token` 的方法来进行除`/login`请求的登录认证

> 我们这里要首先禁用一下`springsecurity`的`session`机制
>
> ```java
>         // 禁用session
> http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
> ```

整体思路流程就是我们首次登录的时候，根据`username`生成`token`然后返回给前端，之后前端每次请求的时候将`token`放在`Header:Authorization`中。我们设置`loginFilter`，在请求之前对`token`进行验证，若解析成功则标明这个用户登录过，那么我们设置`authentication`放到上下文中，如果没有携带`token`或者解析失败，则抛出异常。

```java
//      请求头冲获取token
//      放在'Authorization'中
        Claims claims =jwtManager.parse(httpServletRequest.getHeader("Authorization"));
//      如果不为空，则为上下文添加authentication
        if(claims!=null){
//          获取subject，即username  
            String username = claims.getSubject();
            UserDetails user = userService.loadUserByUsername(username);           
//          设置authentication         
            Authentication authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(),user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
```

> `Claims`为`token`的载荷对象，包括了`token`中的具体信息，例如签发时间，过期时间，`subject`等等。

这里`jwtManager`即为`token`的加密解密工具类

```java
@Slf4j
@Component
public class JWTManager {
//	设置秘钥，这里通过配置文件中的值获取
    @Value("${security.jwt.securityKey}")
    private String securityKey;

//  设置过期时间
    private Duration expiration = Duration.ofDays(1);
    /**
     * 通过用户名加密
     * @param name
     * @return
     */
    public String generate(String name){
        Date expiraDate =new Date(System.currentTimeMillis() + expiration.toMillis());

        //构建token
        return Jwts.builder()
            	//过期时间
                .setExpiration(expiraDate)
            	//设置subject
                .setSubject(name)
            	//设置签发时间
                .setIssuedAt(new Date())
            	//设置加密算法和秘钥
                .signWith(SignatureAlgorithm.HS512,securityKey)
                .compact();
    }

    /**
     * 解密，成功返回claims对象，失败返回null
     * claims为对象存储token有效信息的载荷
     * @param token
     * @return
     */
    public Claims parse(String token){
//      空字符串直接返回null
//      代表当前没有携带token
        if(!StringUtils.hasLength(token)){
            return null;
        }

//      非空字符串则解析
        Claims claims = null;

        try{
            claims = Jwts.parser()
                    .setSigningKey(securityKey)
                    .parseClaimsJws(token)
                    .getBody();
        }
        catch(JwtException e){
            log.error("解析失败",e.toString());
        }
        return claims;
    }
}
```

我们执行一下登录操作

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210615181428.png" style="zoom:80%;" />

可以看到`"token"`即为我们生成的`token`，这样我们下次请求的时候带上这个`token`即可以认证成功。

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210615181634.png" style="zoom:80%;" />

接下来我们改变一下`Authorization`中的值看看

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210615181729.png" style="zoom:80%;" />

可以很明显看到返回了没有登录的视图对象。

