package com.cwh.springboot.springboot_mybatis.controller;

import com.cwh.springboot.springboot_mybatis.dao.entity.Customer;
import com.cwh.springboot.springboot_mybatis.service.CustomerService;
import com.cwh.springboot.springboot_mybatis.vo.CustomerVO;
import com.cwh.springboot.springboot_mybatis.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author cwh
 * @date 2021/6/8 13:58
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/getall")
    public ResultVO<List<Customer>> getAll(){
        List<Customer> customerArrayList = customerService.getAll();
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList();
        for(Customer customer:customerArrayList){
            CustomerVO customerVO = new CustomerVO(customer);
            customerVOLinkedList.add(customerVO);
        }
        ResultVO<List<Customer>> resultVO = new ResultVO(customerVOLinkedList);
        return resultVO;
    }


    @PostMapping("/add")
    public ResultVO add(@RequestParam("name")String name,@RequestParam("age")Integer age){
        Customer customer = new Customer();
        customer.setName(name);
        customer.setAge(age);
        customerService.add(customer);
        ResultVO resultVO = new ResultVO();
        return resultVO;
    }

    @GetMapping("/search")
    public ResultVO searchByName(@RequestParam("name")String name){
        Customer customer = customerService.searchByName(name);
        ResultVO resultVO = new ResultVO(new CustomerVO(customer));
        return resultVO;
    }

    @PutMapping("/update")
    public ResultVO  updateById(@RequestParam("id")Long id,@RequestParam("name")String name,
                                @RequestParam("age") Integer age){
        Customer customer = new Customer();
        customer.setId(id);
        customer.setAge(age);
        customer.setName(name);
        customerService.update(customer);
        return new ResultVO();
    }


    @DeleteMapping("/delete")
    public ResultVO deleteAll(){
        customerService.deleteAll();
        return new ResultVO();
    }

    @DeleteMapping(value = "/delete",params = {"id"})
    public ResultVO deleteById(@RequestParam(value = "id",required = true)Integer id){
        customerService.deleteById(id);
        return new ResultVO();
    }
}
