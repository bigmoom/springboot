package com.cwh.springboot.springsecurity.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author cwh
 * @date 2021/6/11 18:04
 */
@Data
@TableName(value = "role")
public class Role {
    @TableId(type = IdType.INPUT)
    private  Long id;

//  身份名称（超级管理员/数据管理员。。。）
    @TableField(value = "role_name")
    private String name;
}
