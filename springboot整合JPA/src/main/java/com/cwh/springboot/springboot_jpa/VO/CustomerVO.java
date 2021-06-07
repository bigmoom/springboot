package com.cwh.springboot.springboot_jpa.VO;

import com.cwh.springboot.springboot_jpa.dao.entity.Customer;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author cwh
 * @date 2021/6/7 16:54
 */

@Data
public class CustomerVO {

    @JsonProperty(value = "id")
    private Integer id;
    @JsonProperty(value = "name")
    private String name;
    @JsonProperty(value = "age")
    private Integer age;
    @JsonProperty(value = "createTime")
    private String createTime;
    @JsonProperty(value = "modifiedTime")
    private String modifiedTime;

    public CustomerVO setByCustomer(Customer customer){
        CustomerVO customerVO = new CustomerVO();
        customerVO.setId(customer.getId());
        customerVO.setName(customer.getName());
        customerVO.setAge(customer.getAge());
        customerVO.setCreateTime(customer.getCreateTime().toString());
        customerVO.setModifiedTime(customer.getModifiedTime().toString());
        return customerVO;
    }
}
