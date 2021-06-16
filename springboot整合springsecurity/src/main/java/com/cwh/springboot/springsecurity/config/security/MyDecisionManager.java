package com.cwh.springboot.springsecurity.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author cwh
 * @date 2021/6/16 9:42
 */
@Slf4j
@Component
public class MyDecisionManager implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object o, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
//        如果所需权限为空则代表无须授权
        if(configAttributes.isEmpty()){
            return;
        }

        log.info("=========DecisionManager==========");
//        判断授权规则是否与用户具有权限匹配
        for(ConfigAttribute ca: configAttributes){
            for(GrantedAuthority authority : authentication.getAuthorities()){
//                匹配上了则代表有权限
                if(Objects.equals(authority.getAuthority(),ca.getAttribute())){
                    return;
                }
            }
        }

//        走到这里说明没有权限
        throw new AccessDeniedException("没有权限");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return false;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
