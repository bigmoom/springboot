package com.cwh.springboot.springsecurity.config.security;

import com.cwh.springboot.springsecurity.service.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author cwh
 * @date 2021/6/15 9:53
 */
@Slf4j
@Component
public class LogFilter extends OncePerRequestFilter {

    @Autowired
    private JWTManager jwtManager;

    @Autowired
    private UserServiceImpl userService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        log.info("==========loginFilter===========");

//      请求头冲获取token
//      放在'Authorization'中
        Claims claims =jwtManager.parse(httpServletRequest.getHeader("Authorization"));
//      如果不为空，则为上下文添加authentication
        if(claims!=null){
            String username = claims.getSubject();
            UserDetails user = userService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(),user.getAuthorities());
//          设置authentication
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);

    }
}
