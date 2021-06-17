package com.cwh.springboot.authenticaiton_code_client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**授权服务器配置
 * @author cwh
 * @date 2021/6/17 13:42
 */
@Configuration
@EnableAuthorizationServer
public class MyAuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * EndPoint的安全配置与约束
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//               允许表单认证
//               请求/oauth/token的，如果配置了支持allowFormAuthenticationForClients的，且url中有client_id和client_secret的会走ClientCredentialsTokenEndpointFilter
        security.allowFormAuthenticationForClients()
//                开启/oauth/check_token 验证端口认证权限访问
                .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 配置客户端
     * @param clients
     * @throws Exception
     */
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

    /**
     * 注入Endpoint相关配置
     * @param endpoints
     * @throws Exception
     */
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

