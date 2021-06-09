package com.cwh.springboot.springboot_mybatisplus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cwh.springboot.springboot_mybatisplus.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatisplus.dao.mapper.CustomerMapper;
import com.cwh.springboot.springboot_mybatisplus.service.CustomerService;
import com.cwh.springboot.springboot_mybatisplus.vo.CustomerVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/9 9:49
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {


    @Override
    public IPage<CustomerVO> selectPage(Page<CustomerVO> page) {
//        设置条件构造器
        QueryWrapper<CustomerVO> queryWrapper = new QueryWrapper<>();
        return baseMapper.selectPage(page,queryWrapper);
    }

    @Override
    public void addCustomer(Customer customer) {
        baseMapper.addCustomer(customer);
    }

    @Override
    public Customer getCustomerById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void deleteById(Integer id) {
        baseMapper.deleteById(id);
    }

    @Override
    public Integer updateCustomer(Customer customer) {
        return baseMapper.updateById(customer);
    }

    @Override
    public List<Customer> selectByAge(Integer age1,Integer age2) {
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<Customer>();
        queryWrapper.between("age",age1,age2);
        queryWrapper.orderByAsc("age");
        return baseMapper.selectByAge(queryWrapper);
    }


}
