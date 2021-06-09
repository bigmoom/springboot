package com.cwh.springboot.springboot_mybatisplus.vo;

import com.cwh.springboot.springboot_mybatisplus.dao.entity.Customer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author cwh
 * @date 2021/6/9 9:25
 */
@Data
public class CustomerVO {

    private Long id;
    private String name;
    private Integer age;

    @JsonProperty(value = "creat_time")
    private Date createTime;
    @JsonProperty(value = "modify_time")
    private Date modifyTime;

    public CustomerVO(){};

    public CustomerVO(Customer customer){
        this.age = customer.getAge();
        this.name = customer.getName();
        this.id = customer.getId();
        this.createTime = customer.getCreateTime();
        this.modifyTime = customer.getModifyTime();
    }

}
