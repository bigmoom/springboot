package com.cwh.springboot.redis.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author cwh
 * @date 2021/6/21 11:55
 */
@Component
public class MyKeyGenerator implements KeyGenerator {

//    定义项目前缀
    private String prefix = "redis";

    @Override
    public Object generate(Object target, Method method, Object... objects) {
        char sp =':';
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix);
        stringBuilder.append(sp);
//        类名
        stringBuilder.append(target.getClass().getSimpleName());
        stringBuilder.append(sp);
//        方法名
        stringBuilder.append(method.getName());
        stringBuilder.append(sp);
//        参数名
        if(objects.length>0){
            for(Object object: objects){
                stringBuilder.append(object);
            }
        }
        else {
            stringBuilder.append("0");
        }
        return stringBuilder.toString();
    }
}
