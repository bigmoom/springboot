package com.cwh.springboot.redis.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.cwh.springboot.redis.model.entity.Customer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/18 16:39
 */
public interface CustomerMapper extends BaseMapper<Customer> {


    Integer addCustomer(@Param("customer") Customer customer);

    List<Customer> selectByAge(@Param("ew") Wrapper<Customer> Wrapper);

}
