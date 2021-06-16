package com.cwh.springboot.springsecurity.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;


/**
 * @author cwh
 * @date 2021/6/16 10:18
 */
@Slf4j
@Component
public class AuthFilter extends AbstractSecurityInterceptor implements Filter {

//    注入自定义securityMetadataSouce
    @Autowired
    private SecurityMetadataSource securityMetadataSource;

//    实现abstract方法，将mysecuritymetadatasource注入decisionManager中
    @Override
    public SecurityMetadataSource obtainSecurityMetadataSource(){
        return this.securityMetadataSource;
    }

//    注入我们自定义的decisionManager
    @Autowired
//    @Autowired注解在方法上表示在启动时先运行该方法，如果有返回值将返回值放到容器中
//    这里自动装配会自动在容器中获取AccessDecisionManager的实现类然后运行
    @Override
    public void setAccessDecisionManager(AccessDecisionManager accessDecisionManager){
        super.setAccessDecisionManager(accessDecisionManager);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        FilterInvocation filterInvocation = new FilterInvocation(servletRequest,servletResponse,filterChain);
        InterceptorStatusToken token = super.beforeInvocation(filterInvocation);
        try{
            filterInvocation.getChain().doFilter(filterInvocation.getRequest(),filterInvocation.getResponse());
        } finally {
            super.afterInvocation(token,null);
        }
    }

    @Override
    public Class<?> getSecureObjectClass() {
        return FilterInvocation.class;
    }

    @Override
    public void init(FilterConfig filterConfig){}

    @Override
    public void destroy(){}
}
