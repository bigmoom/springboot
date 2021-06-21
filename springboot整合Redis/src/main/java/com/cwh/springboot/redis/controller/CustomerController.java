package com.cwh.springboot.redis.controller;

import com.cwh.springboot.redis.model.entity.Customer;
import com.cwh.springboot.redis.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cwh
 * @date 2021/6/21 10:28
 */
@RestController
@RequestMapping("/customer")
@Slf4j
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/getall")
    public void getAll(){
        List<Customer> customerList =customerService.getAll();
    }

    @GetMapping("/get")
    public Customer getById(@RequestParam("id")Integer id) {
        log.info("================测试缓存==================");
        return customerService.getCustomerById(id);
    }

    @PostMapping("/add")
    public Customer addCustomer(@RequestBody Customer customer){
        return customerService.addCustomer(customer);
    }

    @PutMapping("/update")
    public Customer updateCustomer(@RequestBody Customer customer){
        return customerService.updateCustomer(customer);
    }

    @DeleteMapping("/delete")
    public void deleteById(@RequestParam("id")Integer id){
        customerService.deleteById(id);
    }


    @GetMapping(value = "/get", params = "name")
    public Customer getByName(@RequestParam("name")String name){
        return customerService.getByName(name);
    }
}



