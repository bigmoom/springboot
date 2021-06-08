package com.cwh.springboot.springboot_mybatis.service;

import com.cwh.springboot.springboot_mybatis.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatis.vo.ResultVO;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/8 14:16
 */
public interface CustomerService {

    public List<Customer> getAll();

    public Customer searchByName(String name);

    public Integer add(Customer customer);

    public Integer update(Customer customer);

    public Integer deleteAll();

    public Integer deleteById(Integer id);
}
