package com.cwh.springboot.springboot_mybatisplus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cwh.springboot.springboot_mybatisplus.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatisplus.service.CustomerService;
import com.cwh.springboot.springboot_mybatisplus.vo.CustomerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.util.List;

/**
 * @author cwh
 * @date 2021/6/9 9:57
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

//    分页查询
    @GetMapping("/page/{current}")
    public IPage<CustomerVO> getPage(@PathVariable("current")int current){
//        分页参数（为mybatis-plus中page，与springdata domain中不同）
//        传入当前
        Page<CustomerVO> page = new Page<>(current,5);

//        设置排序序列
        OrderItem orderItem = new OrderItem();
        orderItem.setColumn("create_time");
//        倒叙排列
        orderItem.setAsc(false);
        page.addOrder(orderItem);

        return customerService.selectPage(page);
    }

//    添加用户
    @PostMapping("/add")
    public void add(@RequestParam("name")String name, @RequestParam("age")Integer age){
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAge(age);
        customerService.addCustomer(customer);
    }

//    查询
    @GetMapping("/get")
    public CustomerVO getCustomerById(@RequestParam("id")Integer id){
        Customer customer = customerService.getCustomerById(id);
        return new CustomerVO(customer);
    }

//    根据年龄大小查询
    @GetMapping(value = "/get", params = {"age1","age2"})
    public List<Customer> selectCustomerByAge(@RequestParam("age1")Integer age1, @RequestParam("age2")Integer age2){
        List<Customer> customerList = customerService.selectByAge(age1, age2);
        return customerList;
    }

//    删除
    @DeleteMapping("/delete")
    public void deleteById(@RequestParam("id")Integer id){
        customerService.deleteById(id);
    }
}
