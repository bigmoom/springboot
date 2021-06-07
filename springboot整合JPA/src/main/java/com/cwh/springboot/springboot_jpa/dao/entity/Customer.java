package com.cwh.springboot.springboot_jpa.dao.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * @author cwh
 * @date 2021/6/7 16:21
 */
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Customer {

//  主键
    @Id
//  自增
//  自增策略由数据库控制
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    private Integer age;
//  自动修改创建更新时间
    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date modifiedTime;
}
