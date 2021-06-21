package com.cwh.springboot.redis.controller;

import com.cwh.springboot.redis.model.entity.Customer;
import com.cwh.springboot.redis.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cwh
 * @date 2021/6/21 15:37
 */
@RestController
@RequestMapping("/data")
@Slf4j
public class RedisController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CustomerService customerService;

    /**
     * 添加<NAME,CUSTOMER>的数据
     * @param name
     * @return
     */
    @PostMapping("/add")
    public Customer addCustomer(@RequestParam("name")String name){
        Customer customer = customerService.getByName(name);
        redisTemplate.opsForValue().set(name,customer);
        return customer;
    }

}
