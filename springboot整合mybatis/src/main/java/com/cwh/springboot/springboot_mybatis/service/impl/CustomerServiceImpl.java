package com.cwh.springboot.springboot_mybatis.service.impl;

import com.cwh.springboot.springboot_mybatis.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatis.dao.mapper.CustomerMapper;
import com.cwh.springboot.springboot_mybatis.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/8 14:16
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public List<Customer> getAll() {
        return customerMapper.getAllCustomer();
    }

    @Override
    public Customer searchByName(String name) {
        return customerMapper.getCustomerByName(name);
    }

    @Override
    public Integer add(Customer customer) {
        return customerMapper.addCustomer(customer);
    }

    @Override
    public Integer update(Customer customer) {
        return customerMapper.updateCustomer(customer);
    }

    @Override
    public Integer deleteAll() {
        return customerMapper.deleteAllCustomer();
    }

    @Override
    public Integer deleteById(Integer id) {
        return customerMapper.deleteCustomerById(id);
    }
}
