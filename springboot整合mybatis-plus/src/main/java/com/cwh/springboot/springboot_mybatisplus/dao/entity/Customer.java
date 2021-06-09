package com.cwh.springboot.springboot_mybatisplus.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author cwh
 * @date 2021/6/9 8:59
 */
@Data
@Accessors(chain = true)
@TableName("customer")
public class Customer {

//    设置主键，主键生成策略
//    AUTO为数据库自增
    @TableId(type = IdType.AUTO)
    private Long id;

//    @TableField(value="",exist=true)
//    映射非主键字段 value字段名 exist 标明该属性是否在数据库中
    @TableField(value = "name")
    private String name;

    private Integer age;

    @TableField(value = "create_time",fill = FieldFill.INSERT)
//    fill 自动填充策略
//    INSERT 插入式填充，UPDATE 更新时填充,INSERT_UPDATE插入更新时填充
    private Date createTime;

    @TableField(value = "modify_time",fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;
}
