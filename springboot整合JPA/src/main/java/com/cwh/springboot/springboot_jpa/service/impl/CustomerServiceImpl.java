package com.cwh.springboot.springboot_jpa.service.impl;

import com.cwh.springboot.springboot_jpa.dao.entity.Customer;
import com.cwh.springboot.springboot_jpa.dao.repository.CustomerRepository;
import com.cwh.springboot.springboot_jpa.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

/**
 * @author cwh
 * @date 2021/6/7 16:31
 */
@Service
public class CustomerServiceImpl implements CustomerService {

//    注入数据仓库
    @Autowired
    private CustomerRepository customerRepository;

//    分页操作
    @Override
    public Page<Customer> getAll(Integer pageNum, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNum,pageSize);

        Page<Customer> page = customerRepository.findAll(pageable);

        return page;
    }

//    对数据库修改之后的保存操作
    @Override
    public Customer save(Customer customer) {
       return customerRepository.save(customer);
    }

//    查询操作
    @Override
    public LinkedList<Customer> searchByName(String name) {
        return customerRepository.findByName(name);
    }


}
