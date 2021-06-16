package com.cwh.springboot.springsecurity.config;

import com.cwh.springboot.springsecurity.config.security.AuthFilter;
import com.cwh.springboot.springsecurity.config.security.LogFilter;
import com.cwh.springboot.springsecurity.config.security.MyDeniedHandler;
import com.cwh.springboot.springsecurity.config.security.MyEntryPoint;
import com.cwh.springboot.springsecurity.service.UserService;
import com.cwh.springboot.springsecurity.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

/**
 * @author cwh
 * @date 2021/6/11 17:07
 */
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LogFilter loginFilter;

    @Autowired
    private AuthFilter authFilter;

    /**
     * 配置http请求
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//    关闭csrf和frameOptions
        http.csrf().disable();
        http.headers().frameOptions().disable();

//      开启跨域
        http.cors();

//
        http.authorizeRequests()
//               配置前端跨域联调
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
//              /login接口不需要权限验证
                .antMatchers("/login").permitAll()
//              其他接口都需要权限验证
                .antMatchers("/**").authenticated()
//              指定登录认证和权限验证错误处理器
                .and().exceptionHandling()
                .authenticationEntryPoint(new MyEntryPoint())
                .accessDeniedHandler(new MyDeniedHandler());

        // 禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//        添加登录认证和权限验证过滤器
        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(authFilter,FilterSecurityInterceptor.class);
    }

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

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
