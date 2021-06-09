package com.cwh.springboot.springboot_mybatisplus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cwh.springboot.springboot_mybatisplus.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatisplus.vo.CustomerVO;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/9 9:47
 */
public interface CustomerService extends IService<Customer> {
//  分页
    IPage<CustomerVO> selectPage(Page<CustomerVO> page);
//  添加用户
    void addCustomer(Customer customer);
//  通过用户id查询
    Customer getCustomerById(Integer id);
//  通过用户id删除
    void deleteById(Integer id);
//  更新
    Integer updateCustomer(Customer customer);
//  查询大于age1,小于age2的用户
    List<Customer> selectByAge(Integer age1,Integer age2);
}
