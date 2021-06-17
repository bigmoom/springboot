# SpringBoot使用security实现OAuth2

### OAuth2

`OAuth`是一个开放标准，允许用户授权地方应用访问他们存储在另外的服务提供者上的信息，而不需要将用户名和密码提供给第三方应用或者分享他们数据的所有内容。

我们从一个常见的例子来看：

我们打王者，第一次登录的时候要求我们选择微信登录还是`QQ`登录，这时假设我们点击`QQ`登录，那么就会跳转到一个**认证界面**，询问我们是否同意王者使用`QQ`的数据，例如好友列表等等。当我们点击**同意**之后就会**跳转回**王者，之后进入王者我们可以发现好友列表内容就是我们的`QQ`中的好友列表。并且过段时间不登录之后，我们会发现又要**再次认证**，这是因为之前的认证令牌过期了，需要重新申请。

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617102152.png" style="zoom:80%;" />

这就是`OAuth`的一个大概思路流程

1. 客户端要求用户给予授权
2. 用户同意授权
3. 客户端通过获得授权向认证服务器申请`token`
4. 认证服务器对客户端进行认证，通过认证之后发放`token`
5. 客户端可以通过`token`向资源服务器申请资源
6. 资源服务器确认通过之后，向客户端开放资源

`OAuth2`又分为四种不同的授权模式（简化模式和密码模式由于安全度太低已被遗弃）：

1. **授权码模式**：这是最常用也是安全度最高的一种模式，通过获取授权码增加安全性
2. **~~简化模式~~**：直接申请令牌并且返回令牌，主要用于申请端只有前端没有服务器的情况，例如微信小程序
3. **~~密码模式~~**：需要用户提供账号密码，通过账号密码获取`token`，这种模式安全度极低，不推荐
4. **客户端模式**：与用户无关的一种模式，直接是服务器之间的通信，例如内部系统间的`API`调用

接下来主要讲讲授权码这个核心授权模式

#### 授权码模式

授权码模式`authorization code`，指的是客户端首先向认证服务器申请一个授权码，然后通过该授权码再去向授权服务器申请`token`

```
     +----------+
     | Resource |
     |   Owner  |
     |          |
     +----------+
          ^
          |
         (B)
     +----|-----+          Client Identifier      +---------------+
     |         -+----(A)-- & Redirection URI ---->|               |
     |  User-   |                                 | Authorization |
     |  Agent  -+----(B)-- User authenticates --->|     Server    |
     |          |                                 |               |
     |         -+----(C)-- Authorization Code ---<|               |
     +-|----|---+                                 +---------------+
       |    |                                         ^      v
      (A)  (C)                                        |      |
       |    |                                         |      |
       ^    v                                         |      |
     +---------+                                      |      |
     |         |>---(D)-- Authorization Code ---------'      |
     |  Client |          & Redirection URI                  |
     |         |                                             |
     |         |<---(E)----- Access Token -------------------'
     +---------+       (w/ Optional Refresh Token)
```

步骤如下：

**A**  用户访问客户端，客户端将用户导向认证服务器，并且携带重定向`URI`

```js
https://authorization-server.com/auth?
response_type=code
&client_id=CLIENT_ID
&redirect_uri=REDIRECT_URI
&scope=photos
&state=1234zyx
&code_challenge=CODE_CHALLENGE
&code_challenge_method=S256
```

`response_type=code` 表示授权类型为授权码模式

`client_id` 表示客户端`ID`,第一次创建应用的时候获得

`redirect_uri` 表示重定向`URI`用户在认证完成之后将用户返回到特定`URI` 

`scope` 表示申请的权限范围，例如`READ`

`state` 应用随机指定的值，用于后期验证

`code_challenge` `code_challenge=transform(code_verifier,[Plain|S256])`  

如果`method=Plain`，那么`code-challenge=code_verifier`

如果`method=S256`，那么`code_challenge`等于`code_verifier`的`Sha256`哈希

在授权码请求中带上`code_challenge`以及`method`，这两者与服务器颁发的授权码绑定。

`code_verifier`为客户端生成一个的随机字符串

客户端在用授权码换取`token`时，带上初始生成的`code verifier`，根据绑定的方法进行计算，计算结果与`code_challenge`相比，如果一致再颁发`token`

`code_challenge_method=S256` 标明使用`S256 Hashing`方法

**B** 用户选择是否对客户端授权

**C** 授权之后，认证服务器将用户导向之前传入的重定向`URI`，并且附上授权码

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617105249.png" style="zoom:80%;" />

如果用户点击了`Allow`了，那么服务器将重定向并且附上授权码

```js
https://example-app.com/cb?code=AUTH_CODE_HERE&state=1234zyx
```

`code`即为授权码，授权码有效期很短，一般为10分钟，并且客户端只能使用一次。该码与客户端`ID`和重定向`URI`是一对一关系

`state`之前传入的`state`

我们首先要比较传入的`state`与之前的`state`是否相同（之前的`state`可以存在`cookie`中），用于确认没有被劫持。

**D** 客户端收到授权码后，附上重定向`URI`以及授权码，向认证服务器申请`token`（这一步是在客户端的后台服务器上完成，对用户不可见）

客户端向认证服务器发送申请`token`的`HTTP`请求

```js
POST https://api.authorization-server.com/token
  grant_type=authorization_code&
  code=AUTH_CODE_HERE&
  redirect_uri=REDIRECT_URI&
  client_id=CLIENT_ID&
  code_verifier=CODE_VERIFIER
```

`grant_tyoe`标明为授权码模式

`code` 之前收到的授权码

`redirect_uri` 重定向`URI`，必须与一开始发送的重定向`URI`一样

`client_id` 客户端`ID`，也必须和之前发送的一样

`code_verifier`  之前随机生成的字符串，服务器根据之前传入的`code-challenge`的`method`进行计算，看是否以之前传入的`code_challenge`相同，相同才会颁发`token`

**E** 认证服务器认证授权码等信息，确认无误后向客户端发送`token`和`refresh token`（可选）

通过认证后，服务器发送包含`token`的`HTTPResponse`

```json
     {
       "access_token":"2YotnFZFEjr1zCsicMWpAA",
       "token_type":"bear",
       "expires_in":3600,
       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
     }
```

`access_token` 表示访问令牌

`token_type` 表示`token`类型，可以是`bear`也可以是`mac`

`expires_in` 表示过期时间，单位为秒

`refresh_token`表示更新令牌，用来获取下次的访问令牌。即当`token`过期的时候，向服务器发送请求，告知`token`过期并且将`token`更新为`refresh_token`中的值



### SpringBoot +springsecurity 实现OAuth2

[官方文档](https://projects.spring.io/spring-security-oauth/docs/oauth2.html)

`springsecurity`实现`OAuth2`分为两个服务，`Authorization Server `和`Resource Server`分别作为授权服务器和资源服务器

#### Authorization Server Configuration

正如上面`OAuth2`流程中提到，授权服务器主要作用便是验证客户端，拉起授权页面，用户授权之后通过重定向`URI`携带授权码返回，之后根据授权码验证客户端，发放令牌`Access Token`

`springboot `中，我们在配置类上加上`@EnableAuthorizationServer`并且实现`AuthorizationServerConfigurer`

>  也可以直接继承`springsecurity`提供的`AuthorizationServerConfigurerAdapter`

```java
@Configuration
@EnableAuthorizationServer
public class MyAuthorizationConfig extends AuthorizationServerConfigurerAdapter 
```

配置类中，我们可以通过复写三个不同的`configure`完成对于授权服务器的所有配置

##### ClientDetailsServiceConfigurer

配置客户端信息

```java
    @Override
    public void configure(ClientDetailsServiceConfigurer clients)throws Exception {
//      采用内存模式，也可以使用数据库模式
        clients.inMemory()
//                设置client_id
                .withClient("client-a")
//                设置client_secret
                .secret(passwordEncoder.encode(("client-a-secret")))
//                设置授权模式
                .authorizedGrantTypes("authorization_code")
//                设置权限
                .scopes("read")
//                设置当前client可以访问的资源ID
                .resourceIds("resource1")
                //自动授权，无需人工点击
//                .autoApprove(true)
//                重定向URI
                .redirectUris("http://localhost:9000/callback");
    }
```

我们这里采用的是内存模式配置客户端，也可以通过`JDBC`连接数据库配置客户端信息

`withClient`： 配置`clientId` ，授权不仅仅对用户授权，还要对客户端授权，例如我信任谷歌客户端，不信任百度客户端。于是通过`clientId`和`clientSecret`进行验证

`secret`：配置`clientSecret`

`authorizedGrantTypes` ：配置授权模式

`scopes`：配置权限，默认是所有权限

`resourceIds`：配置该`client`具有的资源服务器，每个资源服务器都有一个唯一的资源`ID`，如果客户端访问没有授权的资源服务器会提示没有权限

`autoApprove`：配置是否自动授权，设置`true`即为开启

`redirectUris`：配置重定向`URI`



##### AuthorizationServerSecurityConfigurer

配置`token endpoint`的安全约束，即提供一些安全访问规则和过滤器

```java
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//               允许表单认证
//               对于请求/oauth/token的，如果配置了支持allowFormAuthenticationForClients的，且url中有client_id和client_secret的会走ClientCredentialsTokenEndpointFilter
        security.allowFormAuthenticationForClients()
//                开启/oauth/check_token 验证端口认证权限访问
                .checkTokenAccess("isAuthenticated()");
    }

```

对于类似`/oauth/check_token`或者`/oauth/token_key`这些端点默认是关闭的即`"denyAll()"`

所以说如果要使用这些端点，我们就要对具有某些权限的用户开启

`allowFormAuthenticationForClients()`：开启表单认证，对于端口`/oauth/token`，如果开启此配置，并且`url`中有`client_id`和`client_secret`会触发`ClientCredentialsTokenEndpointFilter`用于校验客户端是否有权限

`checkTokenAccess`：开启端口`/oauth/check_token`，用于资源服务器的将获取的`token`进行验证

> `isAuthenticated()`标明访问用户是通过验证的，类似`permitAll`或者`hasAuthority()`等等

`addTokenEndpointAuthenticationFilter(IntegrationAuthenticationFilter)`：添加过滤去，可以实现自定义认证，例如短信认证等等

`tokenKeyAccess()`：开启`/oauth/token_key`端口



##### AuthorizationServerEndpointConfigurer

`springsecurity-oauth2`默认提供以下端口

`/oauth/authorize`：授权端口

`/oauth/token`：令牌端口

`/oauth/confirm_access`：用户确认授权提交端口

`/oauth/error`：授权服务错误信息端口

`/oauth/check_token`：用于资源服务器访问的令牌解析端口

`/oauth/token_key`：提供公有秘钥端口，如果使用的是`JWT`令牌的话

`AuthorizationServerEndpointConfigurer`提供了一个方法可以配置自定义的端口`URL`链接

`pathMappring(String 该端口默认URL,String 想要替代的URL)`



`AuthorizationServerEndPointsConfigurer`其实是一个装载类，装载`Endpoints`所有相关的类配置（`TokenStore`,`TokenService`，`UserDetailsService`等等）

```java
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)throws Exception{
        endpoints
//                配置tokenstore，默认存在内存里
                .tokenStore(new InMemoryTokenStore())
//                添加authenticationManager用于密码授权方式
//                .authenticationManager()
//                不添加无法使用refresh_token
//                .userDetailsService()
                .allowedTokenEndpointRequestMethods(HttpMethod.POST,HttpMethod.GET);
    }
}
```

`tokenStoren` ：配置`token`存储的位置，默认是存储在内存中，也可以存储在`Redis`等数据库中

`authenticationManager`：用于配置密码授权方式

`userDetailsService`：配置用于使用`refresh_token`

`allowedTokenEndpointRequestMethods`：配置`TokenEndpoint`允许请求方式



##### SecurityConfig

因为只是`demo`项目，所以将授权用户存储在内存中

```java
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
//        设置用户名密码存在内存中
        auth.inMemoryAuthentication()
                .withUser("cwh")
                .password(passwordEncoder().encode("12345"))
                .authorities(Collections.emptyList());
 }
```



#### Resource Server Configuration

资源服务器提供一些受`token`令牌保护的资源

> 资源服务器可以和授权服务器在同一个应用中，也可以是分开为两个不同的应用

`springboot `中，我们在配置类上加上`@EnableResourceServer`并且实现`ResourceServerConfigurer`

> 也可以直接继承`springsecurity`提供的`ResourceServerConfigurerAdapter`

```java
@Configuration
@EnableResourceServer
public class ResourceConfig extends ResourceServerConfigurerAdapter {
```



`ResourceServerConfigurerAdapter`内部关联了`ResourceServerSecurityConfigurer`和`HttpSecurity`

##### ResourceServerSecurityConfigurer

用于资源服务器的配置

```java
    @Override
    public void configure(ResourceServerSecurityConfigurer resources){
//       resourceID: 规定的资源ID
//       stateless：表示是否只允许基于token的身份验证
        resources.resourceId(RESOURCE_ID).stateless(true);
    }
```

`resourceId`：设置资源服务器的`ID`，用于授权服务器中的权限验证

`stateless`：设置该资源服务器是否只允许基于`token`的身份验证，`true`即为只允许`token`

`tokenStore`：设置`token`的存储方式



##### HttpSecurity

这就是我们之前`spring security`中配置的内容，用于配置一些访问规则，这里就不在赘述

```java
    @Override
    public void configure(HttpSecurity http) throws Exception {
//      设置session创建策略 
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
//     所有端口全部需要验证
        http.authorizeRequests()
                .anyRequest().authenticated();
    }
```



#### 运行结果

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617181316.png" style="zoom:80%;" />

我们访问`localhost:8080/oauth/authorize`并且携带`client_id`，`client_secret`，`response_type`

输入账号密码之后

![](https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617181545.png)

可以发现，浏览器重定向到了我们设定的重定向`URI`，并且携带了授权码`code=DIvTGk`

现在我们携带这个授权码访问`localhost:8080/oauth/token`请求`token`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617181833.png" style="zoom:80%;" />

成功拿到`token`

接着我们再来看看我们的资源服务器

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617181942.png" style="zoom:80%;" />

当我们直接访问时，提示`unauthorized`，即没有被授权

现在我们携带上我们刚刚拿到的`token`

<img src="https://typora-cwh.oss-cn-hangzhou.aliyuncs.com/20210617182057.png" style="zoom:80%;" />

发现访问接口成功

