package com.cwh.springboot.springboot_mybatisplus.dao.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cwh.springboot.springboot_mybatisplus.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatisplus.vo.CustomerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/9 9:24
 */
@Repository
public interface CustomerMapper extends BaseMapper<Customer>{

//    分页查询
    IPage<CustomerVO> selectPage(Page<CustomerVO> page, @Param(Constants.WRAPPER)Wrapper<CustomerVO> Wrapper);

    Integer addCustomer(@Param("customer")Customer customer);

    List<Customer> selectByAge(@Param("ew")Wrapper<Customer> Wrapper);
}
