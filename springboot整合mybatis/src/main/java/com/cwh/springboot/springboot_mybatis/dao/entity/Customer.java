package com.cwh.springboot.springboot_mybatis.dao.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author cwh
 * @date 2021/6/8 11:11
 */

@Data
public class Customer {

    private Long id;

    private String name;

    private Integer age;

    private Date createTime;

    private Date modifyTime;

}
