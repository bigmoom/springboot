package com.cwh.springboot.springboot_mybatis.dao.mapper;

import com.cwh.springboot.springboot_mybatis.dao.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/8 11:30
 */
@Mapper
public interface CustomerMapper {

    List<Customer> getAllCustomer();

    Customer getCustomerByName(@Param("name") String name);

    Integer addCustomer(@Param("customer") Customer customer);

    Integer updateCustomer(@Param("customer") Customer customer);

    Integer deleteAllCustomer();

    Integer deleteCustomerById(@Param("id")Integer id);
}
