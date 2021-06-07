package com.cwh.springboot.springboot_jpa.service;

import com.cwh.springboot.springboot_jpa.dao.entity.Customer;
import org.springframework.data.domain.Page;

import java.util.LinkedList;

/**
 * @author cwh
 * @date 2021/6/7 16:31
 */
public interface CustomerService {

    public Page<Customer> getAll(Integer pageNum, Integer pageSize);

    public Customer save(Customer customer);

    public LinkedList<Customer> searchByName(String name);
}
