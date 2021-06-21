package com.cwh.springboot.redis.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cwh.springboot.redis.model.entity.Customer;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/18 18:09
 */
public interface CustomerService extends IService<Customer> {

    List<Customer> getAll();

    Customer getCustomerById(Integer id);

    Customer updateCustomer(Customer customer);

    int deleteById(Integer id);

    Customer addCustomer(Customer customer);

    Customer getByName(String name);

}
