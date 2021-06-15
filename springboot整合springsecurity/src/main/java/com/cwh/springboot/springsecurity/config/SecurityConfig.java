package com.cwh.springboot.springsecurity.config;

import com.cwh.springboot.springsecurity.config.security.LogFilter;
import com.cwh.springboot.springsecurity.config.security.MyEntryPoint;
import com.cwh.springboot.springsecurity.service.UserService;
import com.cwh.springboot.springsecurity.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
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
                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()

                .antMatchers("/login").permitAll()

                .antMatchers("/**").authenticated()

                .and().exceptionHandling().authenticationEntryPoint(new MyEntryPoint());

        // 禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
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
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
