package com.cwh.springboot.redis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cwh.springboot.redis.config.MyKeyGenerator;
import com.cwh.springboot.redis.dao.CustomerMapper;
import com.cwh.springboot.redis.model.entity.Customer;
import com.cwh.springboot.redis.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/18 18:09
 */
@Service
@Slf4j
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private MyKeyGenerator myKeyGenerator;

    @Cacheable(cacheNames = "getall",keyGenerator = "myKeyGenerator" )
    public List<Customer> getAll(){
        return baseMapper.getAll();
    }


    @Cacheable(cacheNames = "customer",key = "#id" )
    public Customer getCustomerById(Integer id) {
        log.info("===========调用方法============");
        return baseMapper.selectById(id);
    }

//  cacheput 即先调用方法，由更新缓存中数据
    @CachePut(cacheNames = "customer",key = "#customer.id")
    public Customer updateCustomer(Customer customer){
        baseMapper.updateById(customer);
        return customer;
    }

    @CachePut(cacheNames = "customer",key = "#customer.id")
    public Customer addCustomer(Customer customer){
        baseMapper.insert(customer);
        return customer;
    }

    @CacheEvict(cacheNames = "customer",key = "#id" ,beforeInvocation = false)
    public int deleteById(Integer id){

        return baseMapper.deleteById(id);
    }

    @Caching(
            cacheable = {
                    @Cacheable(cacheNames = "customer",key = "#name" )
            },
            put = {
                    @CachePut(cacheNames = "customer",key = "#result.id")
            }
    )
    public Customer getByName(String name){
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",name);
        return baseMapper.getByName(queryWrapper);
    }

}
