package com.cwh.springboot.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class RedisApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        stringRedisTemplate.opsForValue().set("abc","测试");

        stringRedisTemplate.opsForList().leftPushAll("qq", list);
        stringRedisTemplate.opsForList().range("qwe",0,-1).forEach(value -> {
            System.out.println(value);
        });
    }

}
