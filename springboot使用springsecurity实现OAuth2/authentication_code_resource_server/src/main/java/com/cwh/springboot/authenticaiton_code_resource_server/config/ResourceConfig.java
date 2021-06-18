package com.cwh.springboot.authenticaiton_code_resource_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;

/**
 * @author cwh
 * @date 2021/6/17 14:32
 */
@Configuration
@EnableResourceServer
public class ResourceConfig extends ResourceServerConfigurerAdapter {

    private static final String RESOURCE_ID = "resource1";

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Primary
    @Bean
    public RemoteTokenServices remoteTokenServices(){
        final RemoteTokenServices remoteTokenServices = new RemoteTokenServices();
//      设置/oauth/check_token端口
        remoteTokenServices.setCheckTokenEndpointUrl("http://localhost:8080/oauth/check_token");
//      设置客户端信息
        remoteTokenServices.setClientId("client-a");
        remoteTokenServices.setClientSecret("client-a-secret");
        return remoteTokenServices;
    }


    @Override
    public void configure(HttpSecurity http) throws Exception {
//      设置session创建策略
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
//     所有端口全部需要验证
        http.authorizeRequests()
                .anyRequest().authenticated();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources){
//       resourceID: 规定的资源ID
//       stateless：表示是否只允许基于token的身份验证
        resources.resourceId(RESOURCE_ID).stateless(true);
    }
}
