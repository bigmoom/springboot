package com.cwh.springboot.springsecurity.config.security;

import com.cwh.springboot.springsecurity.model.entity.Resource;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cwh
 * @date 2021/6/16 9:15
 */
@Slf4j
@Component
public class MySecurityMetadataSource implements SecurityMetadataSource {

    /**
     * 用去存储当前系统所有的url资源
     */
    @Getter
    private static final Set<Resource> RESOURCES = new HashSet<Resource>();

    /**
     * 获取被保护对象所需权限信息
     * @param object
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {

//        通过springsecurity 提供的FilterInvocation将httprequest封装保护起来
        FilterInvocation filterInvocation = (FilterInvocation)object;
        HttpServletRequest request = filterInvocation.getRequest();
        //在RESOURCES中匹配到当前请求，并返回权限id
        for(Resource resource: RESOURCES){
//            数据库中请求资源为 GET:/user 格式
//            冒号前为请求方式，冒号后为请求路径
            String[] url = resource.getUrl().split(":");
//            通过Ant类来比较url是否匹配
            AntPathRequestMatcher ant = new AntPathRequestMatcher(url[1]);
            if(request.getMethod().equals(url[0]) && ant.matches(request)){
                return Collections.singletonList(new SecurityConfig(resource.getId().toString()));
            }
        }
        return null;
    }


    /**
     * 返回了所有定义的权限资源
     * Spring Security会在启动的时候校验每个 ConfigAttribute是否配置正确
     * 暂时不用管
     * @return
     */
    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    /**
     * 用于告知调用者当前SecurityMetadataSource是否支持此类安全对象
     * 只有支持时才能调用getAttributes()
     * 这里改为true就可以使用了
     * @param aClass
     * @return
     */
    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
