package com.cwh.springboot.springboot_jpa.controller;

import com.cwh.springboot.springboot_jpa.VO.CustomerVO;
import com.cwh.springboot.springboot_jpa.VO.ResultVO;
import com.cwh.springboot.springboot_jpa.dao.entity.Customer;
import com.cwh.springboot.springboot_jpa.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author cwh
 * @date 2021/6/7 16:30
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;



    @GetMapping("/getall")
    public ResultVO<LinkedList<CustomerVO>> getAll(@RequestParam("pagenum")Integer pagenum,@RequestParam("pagesize")Integer pagesize) {

        Page<Customer> page = customerService.getAll(pagenum,pagesize);
        List<Customer> customerList = page.getContent();

        ResultVO<LinkedList<CustomerVO>> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("succuss");
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList<CustomerVO>();
        for(Customer customer:customerList){
            CustomerVO customerVO = new CustomerVO().setByCustomer(customer);

        }

        resultVO.setData(customerVOLinkedList);

        return resultVO;
    }

    @PostMapping("/add")
    public ResultVO<CustomerVO> addCustomer(@RequestParam(value="name",required = true)String name,
                                @RequestParam(value="age",required = true)Integer age){

        Customer customer = new Customer();
        customer.setAge(age);
        customer.setName(name);
        customerService.save(customer);

        ResultVO<CustomerVO> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("add success");

        CustomerVO customerVO = new CustomerVO().setByCustomer(customer);

        resultVO.setData(customerVO);
        return resultVO;
    }


    @GetMapping("/search")
    public ResultVO<LinkedList<CustomerVO>> searchByName(@RequestParam(value="name",required = true)String name){

        ResultVO<LinkedList<CustomerVO>> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("search success");
        LinkedList<Customer> customerList = customerService.searchByName(name);
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList();
        for(Customer customer:customerList){
            CustomerVO customerVO = new CustomerVO().setByCustomer(customer);
            customerVOLinkedList.add(customerVO);
        }
        resultVO.setData(customerVOLinkedList);
        return  resultVO;
    }

}
