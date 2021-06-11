package com.cwh.springboot.springsecurity.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author cwh
 * @date 2021/6/11 17:57
 */
@Data
@TableName(value = "resource")
public class Resource {

    @TableId(type = IdType.INPUT)
    private Long id;

//   路径
    @TableField(value = "url")
    private String url;

//  类型，1为接口，0为页面
    @TableField(value = "type")
    private Integer type;
}
