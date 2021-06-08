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
//  注入service接口
    @Autowired
    private CustomerService customerService;

    /**
     * 分页展示
     * @param pagenum 页码（第一页为0）
     * @param pagesize 分页大小
     * @return
     */
    @GetMapping("/getall")
    public ResultVO<LinkedList<CustomerVO>> getAll(@RequestParam("pagenum")Integer pagenum,@RequestParam("pagesize")Integer pagesize) {

        Page<Customer> page = customerService.getAll(pagenum,pagesize);
//        获取页内数据内容
        List<Customer> customerList = page.getContent();

//        设置VO
        ResultVO<LinkedList<CustomerVO>> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("succuss");
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList<CustomerVO>();
        for(Customer customer:customerList){
            CustomerVO customerVO = new CustomerVO().setByCustomer(customer);
            customerVOLinkedList.add(customerVO);
        }

        resultVO.setData(customerVOLinkedList);
        return resultVO;
    }

    /**
     * 添加用户
     * @param name
     * @param age
     * @return
     */
    @PostMapping("/add")
    public ResultVO<CustomerVO> addCustomer(@RequestParam(value="name",required = true)String name,
                                @RequestParam(value="age",required = true)Integer age){

        Customer customer = new Customer();
        customer.setAge(age);
        customer.setName(name);
//        保存修改
        customerService.save(customer);
//        设置VO
        ResultVO<CustomerVO> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("add success");

        CustomerVO customerVO = new CustomerVO().setByCustomer(customer);

        resultVO.setData(customerVO);
        return resultVO;
    }

    /**
     * 通过名字查询
     * 可以有同名存在，所以返回List
     * @param name
     * @return
     */
    @GetMapping("/search")
    public ResultVO<LinkedList<CustomerVO>> searchByName(@RequestParam(value="name",required = true)String name){

        ResultVO<LinkedList<CustomerVO>> resultVO = new ResultVO();
        resultVO.setCode(1);
        resultVO.setMsg("search success");
//        获取查询结果
        LinkedList<Customer> customerList = customerService.searchByName(name);
//        设置VO
        LinkedList<CustomerVO> customerVOLinkedList = new LinkedList();
        for(Customer customer:customerList){
            CustomerVO customerVO = new CustomerVO().setByCustomer(customer);
            customerVOLinkedList.add(customerVO);
        }
        resultVO.setData(customerVOLinkedList);
        return  resultVO;
    }

}
