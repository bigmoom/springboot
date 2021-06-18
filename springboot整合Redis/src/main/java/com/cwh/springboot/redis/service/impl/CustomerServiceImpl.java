package com.cwh.springboot.redis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cwh.springboot.redis.dao.CustomerMapper;
import com.cwh.springboot.redis.model.entity.Customer;
import com.cwh.springboot.redis.service.CustomerService;

/**
 * @author cwh
 * @date 2021/6/18 18:09
 */
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {
}
