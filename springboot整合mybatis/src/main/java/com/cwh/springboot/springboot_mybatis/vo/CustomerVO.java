package com.cwh.springboot.springboot_mybatis.vo;

import com.cwh.springboot.springboot_mybatis.dao.entity.Customer;
import lombok.Data;

import java.util.Date;

/**
 * @author cwh
 * @date 2021/6/8 14:38
 */
@Data
public class CustomerVO {
    private Long id;
    private String name;
    private Integer age;
    private Date createDt;
    private Date modifyDt;

    public CustomerVO(Customer customer){
        this.id = customer.getId();
        this.name = customer.getName();
        this.age = customer.getAge();
        this.createDt = customer.getCreateTime();
        this.modifyDt = customer.getModifyTime();
    }
}
