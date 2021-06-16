package com.cwh.springboot.springsecurity.controller;

import com.cwh.springboot.springsecurity.annotation.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cwh
 * @date 2021/6/16 14:52
 */
@RestController
@Slf4j
@RequestMapping("/data")
@Auth(id = 2000, name = "数据操作")
public class DataController {

    @GetMapping
    @Auth(id=1,name="获取数据")
    public String getData(){
        return "获取数据成功";
    }

    @PostMapping
    @Auth(id=2,name="添加数据")
    public String addData(){
        return "添加数据成功";
    }
}
